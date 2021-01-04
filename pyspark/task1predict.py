import sys
import json
import math
from time import time
from pyspark import SparkConf, SparkContext


# cosine similarity of (user_id, business_id) pair
def cosine_similarity(user_id, business_id, user_profile, item_profile):
    if user_id not in user_profile or business_id not in item_profile:
        return {"user_id": user_id, "business_id": business_id, "sim": 0}
    user_vector = set(user_profile[user_id])
    business_vector = set(item_profile[business_id])
    sim = len(user_vector.intersection(business_vector)) / (math.sqrt(len(user_vector)) * math.sqrt(len(business_vector)))
    return {"user_id": user_id, "business_id": business_id, "sim": sim}


if __name__ == '__main__':
    start_time = time()

    # dataset/test_review.json output/task2.model output/task2.predict
    test_file = sys.argv[1]
    model_file = sys.argv[2]
    output_file = sys.argv[3]

    conf = SparkConf().set("spark.executor.memory", "4g").set("spark.driver.memory", "4g")
    sc = SparkContext(appName="Task1_Predict", conf=conf)

    # parse model_file, get item_profile dict and user_profile dict
    with open(model_file) as model_file:
        model_content = json.loads(model_file.readline())
        user_profile = model_content["user_profile"]
        item_profile = model_content["item_profile"]
    model_file.close()

    predict_user_business = sc.textFile(test_file).map(lambda x: json.loads(x)) \
        .map(lambda x: (x["user_id"], x["business_id"])) \
        .collect()

    result = []
    for elem in predict_user_business:
        # compute cosine similarity of (user_id, business_id) pair
        result.append(cosine_similarity(elem[0], elem[1], user_profile, item_profile))

    with open(output_file, "w+") as output:
        for elem in result:
            if elem["sim"] >= 0.01:
                output.write(json.dumps(elem) + "\n")
    output.close()

    end_time = time()

    print('Duration: %.2f' % (end_time - start_time))
