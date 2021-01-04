import os
import json
import math
from time import time
from itertools import combinations
from pyspark import SparkConf, SparkContext

# os.environ['PYSPARK_PYTHON'] = '/usr/local/bin/python3.6'
# os.environ['PYSPARK_DRIVER_PYTHON'] = '/usr/local/bin/python3.6'


# find the business pairs that have at least 1 co-rated users
# business_star_list : [(b1_index, average_star1), (b2_index, average_star2), ...]
# result             : [((b1_index, b2_index), [(average_star1, average_star2)]), ...]
def candidate_business_pairs(business_star_list):
    result = []
    for pair in combinations(sorted(business_star_list), 2):
        result.append(((pair[0][0], pair[1][0]), [(pair[0][1], pair[1][1])]))
    return result


# co_ratings_stars = [(star1_1, star2_1), (star1_2, star2_2), (star1_3, star2_3), ...]
# return co-ratings Pearson similarity of (business1, business2) pair
def co_ratings_Pearson_similarity(co_ratings_stars):
    avg_star1 = avg_star2 = 0
    for star1, star2 in co_ratings_stars:
        avg_star1 += star1
        avg_star2 += star2
    avg_star1 /= len(co_ratings_stars)
    avg_star2 /= len(co_ratings_stars)

    w_numerator = w_denominator_first = w_denominator_second = 0.0
    for star1, star2 in co_ratings_stars:
        a = star1 - avg_star1
        b = star2 - avg_star2
        w_numerator += a * b
        w_denominator_first += a * a
        w_denominator_second += b * b
    w_denominator = math.sqrt(w_denominator_first) * math.sqrt(w_denominator_second)
    return 1 if w_denominator == 0 else w_numerator / w_denominator


if __name__ == '__main__':
    start_time = time()

    # train_file = "../resource/asnlib/publicdata/train_review.json"
    train_file = "../dataset2/train_review.json"
    model_file = "task.model"

    conf = SparkConf().set("spark.executor.memory", "4g").set("spark.driver.memory", "4g")
    sc = SparkContext(appName="Task3_Train", conf=conf)

    input_rdd = sc.textFile(train_file) \
        .map(lambda x: json.loads(x)) \
        .map(lambda x: (x["business_id"], x["user_id"], x["stars"]))

    business_index_rdd = input_rdd.map(lambda x: x[0]).distinct().zipWithIndex()
    business_index_dict = business_index_rdd.collectAsMap()
    index_business_dict = business_index_rdd.map(lambda x: (x[1], x[0])).collectAsMap()

    user_index_rdd = input_rdd.map(lambda x: x[1]).distinct().zipWithIndex()
    user_index_dict = user_index_rdd.collectAsMap()
    index_user_dict = user_index_rdd.map(lambda x: (x[1], x[0])).collectAsMap()

    # Step 1. compute average star for every (business, user) pair
    # Step 2. get every user's all (business, star) pairs
    # Step 3. combine (b1, s1) and (b2, s2) of every user into ((b1, b2), (s1, s2))
    # Step 4. find the business pairs that have at least 3 co-rated users
    # Step 5. compute co-ratings Pearson similarity for every (business1, business2) pair
    #
    # (business_id, user_id, star)
    # => ((b_index, u_index), [star])
    # => ((b_index, u_index), [list of stars])
    # => (u_index, (b_index, avg_star))
    # => (u_index, [(b_index, star)])
    # => (u_index, [(b1_index, star1), (b2_index, star2), ...])
    # => [((b1_index, b2_index), [(star1, star2)]), ...]
    # => ((b1_index, b2_index), [(star1, star2)])
    # => ((b1_index, b2_index), [(star1_1, star2_1), (star1_2, star2_2), (star1_3, star2_3), ...])
    # => ((b1_index, b2_index), sim)
    # => ((b1_id, b2_id), sim)
    business_pairs = input_rdd.map(lambda x: ((business_index_dict[x[0]], user_index_dict[x[1]]), [x[2]])) \
        .reduceByKey(lambda a, b: a + b) \
        .map(lambda x: (x[0][1], (x[0][0], sum(x[1]) / len(x[1])))) \
        .mapValues(lambda x: [x]) \
        .reduceByKey(lambda a, b: a + b) \
        .flatMap(lambda x: candidate_business_pairs(x[1])) \
        .reduceByKey(lambda a, b: a + b) \
        .filter(lambda x: len(x[1]) >= 3) \
        .map(lambda x: (x[0], co_ratings_Pearson_similarity(x[1]))) \
        .filter(lambda x: x[1] > 0) \
        .map(lambda x: (index_business_dict[x[0][0]], index_business_dict[x[0][1]], x[1])) \
        .collect()

    with open(model_file, "w+") as output:
        for index, elem in enumerate(business_pairs):
            if index == len(business_pairs) - 1:
                output.write(json.dumps({'b1': elem[0], 'b2': elem[1], 'sim': elem[2]}))
            else:
                output.write(json.dumps({'b1': elem[0], 'b2': elem[1], 'sim': elem[2]}) + "\n")
    output.close()

    print('Duration: %.2f' % (time() - start_time))
