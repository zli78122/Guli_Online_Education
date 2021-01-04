import sys
import json
from operator import add
from time import time
from pyspark import SparkConf, SparkContext


# predict the rating of (predict_user, predict_business)
#
# (predict_user, business_sim_list) = (u1, [(b3, sim), (b5, sim), (b9, sim)])
#
# user_business_star_dict = {(user, business) : star}
def item_based_prediction(predict_user, business_sim_list, user_business_star_dict, item_based_N):
    w_numerator = w_denominator = 0
    for business, sim in business_sim_list:
        if (predict_user, business) in user_business_star_dict:
            w_numerator += user_business_star_dict[(predict_user, business)] * sim
            w_denominator += sim
            item_based_N -= 1
            if item_based_N == 0:
                break
    return 0 if w_denominator == 0 else w_numerator / w_denominator


# predict the rating of (predict_user, predict_business)
#
#   (u1,           (b1,               [(u3, sim),       (u5, sim),       (u9, sim)]))
# = (predict_user, (predict_business, [(u3, w(u1, u3)), (u5, w(u1, u5)), (u9, w(u1, u9))])
#
# user_business_star_dict = {(user, business) : star}
# user_sum_len_star_dict  = {user : (star_sum, star_len)}
def user_based_prediction(predict_user, predict_business, user_sim_list, user_business_star_dict,
                          user_sum_len_star_dict, user_based_N):

    predict_user_avg = user_sum_len_star_dict[predict_user][0] / user_sum_len_star_dict[predict_user][1]

    w_numerator = w_denominator = 0
    for user, sim in user_sim_list:
        if (user, predict_business) in user_business_star_dict:
            user_star = user_business_star_dict[(user, predict_business)]

            user_star_sum = user_sum_len_star_dict[user][0]
            user_star_len = user_sum_len_star_dict[user][1]
            user_avg = 0 if user_star_len == 1 else (user_star_sum - user_star) / (user_star_len - 1)

            w_numerator += (user_star - user_avg) * sim
            w_denominator += sim

            user_based_N -= 1
            if user_based_N == 0:
                break

    return predict_user_avg if w_denominator == 0 else predict_user_avg + (w_numerator / w_denominator)


if __name__ == '__main__':
    start_time = time()

    # dataset/train_review.json dataset/test_review.json output/task3item.model output/task3item.predict item_based
    # dataset/train_review.json dataset/test_review.json output/task3user.model output/task3user.predict user_based
    train_file = sys.argv[1]
    test_file = sys.argv[2]
    model_file = sys.argv[3]
    output_file = sys.argv[4]
    cf_type = sys.argv[5]
    item_based_N = 30
    user_based_N = 30

    conf = SparkConf().set("spark.executor.memory", "4g").set("spark.driver.memory", "4g")
    sc = SparkContext(appName="Task2_Predict", conf=conf)

    # ((user, business), star)
    user_business_star_rdd = sc.textFile(train_file).map(lambda x: json.loads(x)) \
        .map(lambda x: ((x["user_id"], x["business_id"]), [x["stars"]])) \
        .reduceByKey(lambda a, b: a + b) \
        .map(lambda x: (x[0], sum(x[1]) / len(x[1])))
    # {(user, business) : star}
    user_business_star_dict = user_business_star_rdd.collectAsMap()

    if cf_type == "item_based":
        # (b1, [(b3, sim), (b5, sim), (b9, sim)])
        business_pair_sim = sc.textFile(model_file).map(lambda x: json.loads(x)) \
            .map(lambda x: (x["b1"], x["b2"], x["sim"])) \
            .flatMap(lambda x: [(x[0], [(x[1], x[2])]), (x[1], [(x[0], x[2])])]) \
            .reduceByKey(add) \
            .map(lambda x: (x[0], sorted(x[1], key=lambda _: _[1], reverse=True)))

        # (b1, u1) join (b1, [(b3, sim), (b5, sim), (b9, sim)])
        # => (b1, (u1, [(b3, sim), (b5, sim), (b9, sim)]))
        # => (u1, b1, rating)
        business_user_prediction = sc.textFile(test_file).map(lambda x: json.loads(x)) \
            .map(lambda x: (x["business_id"], x["user_id"])) \
            .join(business_pair_sim) \
            .map(lambda x: (x[1][0], x[0], item_based_prediction(x[1][0], x[1][1], user_business_star_dict, item_based_N))) \
            .filter(lambda x: x[2] > 0) \
            .collect()

        with open(output_file, "w+") as output:
            for user, business, rating in business_user_prediction:
                output.write(json.dumps({'user_id': user, 'business_id': business, 'stars': rating}) + "\n")
        output.close()

    elif cf_type == "user_based":
        # ((user, business), star) => (user, [star]) => (user, [star1, star2, star3, ...])
        # => (user, (star_sum, star_len)) => {user : (star_sum, star_len)}
        user_sum_len_star_dict = user_business_star_rdd.map(lambda x: (x[0][0], [x[1]])) \
            .reduceByKey(lambda a, b: a + b) \
            .map(lambda x: (x[0], (sum(x[1]), len(x[1])))) \
            .collectAsMap()

        # (u1, [(u3, sim), (u5, sim), (u9, sim)])
        user_pair_sim = sc.textFile(model_file).map(lambda x: json.loads(x)) \
            .map(lambda x: (x["u1"], x["u2"], x["sim"])) \
            .flatMap(lambda x: [(x[0], [(x[1], x[2])]), (x[1], [(x[0], x[2])])]) \
            .reduceByKey(add) \
            .map(lambda x: (x[0], sorted(x[1], key=lambda _: _[1], reverse=True)))

        # (u1, b1) join (u1, [(u3, sim), (u5, sim), (u9, sim)])
        # => (u1, (b1, [(u3, sim), (u5, sim), (u9, sim)]))
        # => (u1, b1, rating)
        user_business_prediction = sc.textFile(test_file).map(lambda x: json.loads(x)) \
            .map(lambda x: (x["user_id"], x["business_id"])) \
            .join(user_pair_sim) \
            .map(lambda x: (x[0], x[1][0], user_based_prediction(x[0], x[1][0], x[1][1], user_business_star_dict,
                                                                 user_sum_len_star_dict, user_based_N))) \
            .filter(lambda x: x[2] > 0) \
            .collect()

        with open(output_file, "w+") as output:
            for user, business, rating in user_business_prediction:
                output.write(json.dumps({'user_id': user, 'business_id': business, 'stars': rating}) + "\n")
        output.close()

    end_time = time()

    print('Duration: %.2f' % (end_time - start_time))
