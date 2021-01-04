import os
import sys
import json
from operator import add
from time import time
from pyspark import SparkConf, SparkContext

# os.environ['PYSPARK_PYTHON'] = '/usr/local/bin/python3.6'
# os.environ['PYSPARK_DRIVER_PYTHON'] = '/usr/local/bin/python3.6'


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


if __name__ == '__main__':
    start_time = time()

    # ../dataset2/test_review.json task.predict
    test_file = sys.argv[1]
    output_file = sys.argv[2]
    # train_file = "../resource/asnlib/publicdata/train_review.json"
    train_file = "../dataset2/train_review.json"
    model_file = "task.model"
    item_based_N = 30
    # user_avg_file = "../resource/asnlib/publicdata/user_avg.json"
    user_avg_file = "../dataset2/user_avg.json"
    # business_avg_file = "../resource/asnlib/publicdata/business_avg.json"
    business_avg_file = "../dataset2/business_avg.json"

    user_avg = {}
    try:
        with open(user_avg_file) as user_avg_file:
            user_avg = json.loads(user_avg_file.readline())
    except FileNotFoundError:
        print(f'{user_avg_file} not exists')
    user_avg_file.close()

    business_avg = {}
    try:
        with open(business_avg_file) as business_avg_file:
            business_avg = json.loads(business_avg_file.readline())
    except FileNotFoundError:
        print(f'{business_avg_file} not exists')
    business_avg_file.close()

    conf = SparkConf().set("spark.executor.memory", "4g").set("spark.driver.memory", "4g")
    sc = SparkContext(appName="Task3_Predict", conf=conf)

    # ((user, business), star)
    user_business_star_rdd = sc.textFile(train_file).map(lambda x: json.loads(x)) \
        .map(lambda x: ((x["user_id"], x["business_id"]), [x["stars"]])) \
        .reduceByKey(lambda a, b: a + b) \
        .map(lambda x: (x[0], sum(x[1]) / len(x[1])))
    # {(user, business) : star}
    user_business_star_dict = user_business_star_rdd.collectAsMap()

    # (b1, [(b3, sim), (b5, sim), (b9, sim)])
    business_pair_sim = sc.textFile(model_file).map(lambda x: json.loads(x)) \
        .map(lambda x: (x["b1"], x["b2"], x["sim"])) \
        .flatMap(lambda x: [(x[0], [(x[1], x[2])]), (x[1], [(x[0], x[2])])]) \
        .reduceByKey(add) \
        .map(lambda x: (x[0], sorted(x[1], key=lambda _: _[1], reverse=True)))

    # (b1, u1) join (b1, [(b3, sim), (b5, sim), (b9, sim)])
    # => (b1, (u1, [(b3, sim), (b5, sim), (b9, sim)]))
    # => (u1, b1, rating)
    # => ((u1, b1), rating)
    business_user_prediction_dict = sc.textFile(test_file).map(lambda x: json.loads(x)) \
        .map(lambda x: (x["business_id"], x["user_id"])) \
        .join(business_pair_sim) \
        .map(lambda x: (x[1][0], x[0], item_based_prediction(x[1][0], x[1][1], user_business_star_dict, item_based_N))) \
        .filter(lambda x: x[2] > 0) \
        .map(lambda x: ((x[0], x[1]), x[2])) \
        .collectAsMap()

    all_user_business = sc.textFile(test_file).map(lambda x: json.loads(x)) \
        .map(lambda x: (x["user_id"], x["business_id"])) \
        .collect()

    result = []
    for user, business in all_user_business:
        if (user, business) in business_user_prediction_dict:
            rating = business_user_prediction_dict[(user, business)]
        else:
            if user in user_avg and business in business_avg:
                rating = (user_avg[user] + business_avg[business]) / 2
            elif user in user_avg:
                rating = user_avg[user]
            elif business in business_avg:
                rating = business_avg[business]
            else:
                rating = 3.75
        result.append((user, business, rating))

    with open(output_file, "w+") as output:
        for user, business, rating in result:
            output.write(json.dumps({'user_id': user, 'business_id': business, 'stars': rating}) + "\n")
    output.close()

    print('Duration: %.2f' % (time() - start_time))
