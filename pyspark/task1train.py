import sys
import json
import math
from operator import add
from time import time
import string
from pyspark import SparkConf, SparkContext


def build_stop_word_set(stop_word_set, stopwords_path):
    with open(stopwords_path) as stop_word_file:
        while True:
            stop_word = stop_word_file.readline()[0:-1]
            if not stop_word:
                break
            stop_word_set.add(stop_word)
    stop_word_file.close()


# remove the punctuations, numbers, and stopwords
def format_text(text, remove_character_set, stop_word_set):
    first_filter = []
    second_result = []

    for word in text:
        start = 0
        while start < len(word):
            if word[start] not in remove_character_set:
                break
            else:
                start += 1
        end = len(word) - 1
        while end >= 0:
            if word[end] not in remove_character_set:
                break
            else:
                end -= 1
        if start <= end:
            first_filter.append(word[start:end + 1].lower())

    for word in first_filter:
        if word not in stop_word_set:
            second_result.append(word)

    return second_result


def business_frequent_words(business_words, frequent_word_dict):
    new_word_list = list()
    for word in business_words:
        if word in frequent_word_dict:
            new_word_list.append(frequent_word_dict[word])
    new_word_set = set(new_word_list)
    return new_word_list, new_word_set


def business_TF_IDF(word_list, word_IDF_dict):
    # word count of every word in current document
    word_count = dict()
    # the maximum word count among all words in current document
    max_count = 0
    for word in word_list:
        if word not in word_count:
            word_count[word] = 1
        else:
            word_count[word] += 1
        max_count = max(max_count, word_count[word])

    # word TF-IDF of every word in current document
    word_TF_IDF = dict()
    # convert word count to word TF-IDF
    for word, count in word_count.items():
        word_TF_IDF[word] = count / max_count * word_IDF_dict[word]

    top_200_words = sorted(word_TF_IDF.items(), key=lambda x: x[1], reverse=True)[0:200]
    for i in range(len(top_200_words)):
        top_200_words[i] = top_200_words[i][0]

    return top_200_words


if __name__ == '__main__':
    start_time = time()

    # dataset/train_review.json output/task2.model dataset/stopwords
    train_file = sys.argv[1]
    model_file = sys.argv[2]
    stopwords = sys.argv[3]

    # !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~0123456789
    remove_character_set = set(string.punctuation + string.digits)

    stop_word_set = set()
    build_stop_word_set(stop_word_set, stopwords)

    conf = SparkConf().set("spark.executor.memory", "4g").set("spark.driver.memory", "4g")
    sc = SparkContext(appName="Task1_Train", conf=conf)

    # ============================== BUILD ITEM PROFILE ==============================

    # remove the punctuations, numbers, and stopwords
    # "A B\nC\tD".split() => ["A", "B", "C", "D"]
    # (business_id, text) => (business_id, [word1, word2, word3, ...])
    business_words = sc.textFile(train_file) \
        .map(lambda x: json.loads(x)) \
        .map(lambda x: (x["business_id"], format_text(x["text"].split(), remove_character_set, stop_word_set))) \
        .reduceByKey(add)

    # (business_id, [word1, word2, word3, ...]) => (word) => (word, 1) => (word, sum)
    word_sum = business_words.flatMap(lambda x: x[1]) \
        .map(lambda x: (x, 1)) \
        .reduceByKey(lambda a, b: a + b)
    # compute the total number of all words
    total_word_number = business_words.flatMap(lambda x: x[1]).count()
    # rare_word_threshold = total_word_number * 0.000001 = 34
    rare_word_threshold = int(total_word_number * 0.000001)
    # filter out all rare words, and retain all relatively frequent words
    frequent_word = word_sum.filter(lambda x: x[1] > rare_word_threshold).map(lambda x: x[0])
    frequent_word_dict = frequent_word.zipWithIndex().collectAsMap()

    # (business_id, [word1, word2, word3, ...]) => (business_id, ([list of words], {set of words}))
    business_word_list_set = business_words.map(lambda x: (x[0], business_frequent_words(x[1], frequent_word_dict)))

    # compute IDF of every frequent word
    # N : total number of documents, N = 10253
    N = business_word_list_set.count()
    # word_IDF_dict[i] : the number of documents that mention word i
    # (business_id, ([list of words], {set of words})) => {set of words} => [set of words] => (word)
    # => (word, 1) => (word, sum) => (word, IDF of the word) => {word, IDF of the word}
    word_IDF_dict = business_word_list_set.flatMap(lambda x: list(x[1][1])) \
        .map(lambda x: (x, 1)) \
        .reduceByKey(lambda a, b: a + b) \
        .map(lambda x: (x[0], math.log2(N / x[1]))) \
        .collectAsMap()

    # compute top 200 words with highest TF-IDF scores of every document
    # Note: one document is corresponding to one business
    # (business_id, ([list of words], {set of words})) => (business_id, [top 200 frequent words])
    item_profile = business_word_list_set.map(lambda x: (x[0], business_TF_IDF(x[1][0], word_IDF_dict))).collectAsMap()

    # ============================== BUILD USER PROFILE ==============================
    user_profile = sc.textFile(train_file) \
        .map(lambda x: json.loads(x)) \
        .map(lambda x: (x["user_id"], x["business_id"])) \
        .map(lambda x: (x[0], set(item_profile[x[1]]))) \
        .reduceByKey(lambda a, b: a.union(b)) \
        .map(lambda x: (x[0], list(x[1]))) \
        .collectAsMap()

    # ============================== PRINT RESULT ==============================
    with open(model_file, "w+") as output:
        output.write(json.dumps({'item_profile': item_profile, 'user_profile': user_profile}))
    output.close()

    end_time = time()

    print('Duration: %.2f' % (end_time - start_time))
