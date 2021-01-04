import sys
import json
import math
from operator import add
from time import time
from itertools import combinations
from pyspark import SparkConf, SparkContext


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


def generate_argument_a():
    return [213, 332, 474, 452, 315, 119, 471, 402, 536, 88, 134, 1, 124, 532, 19, 147, 43, 535, 377, 469, 215, 423,
            463, 363, 254, 327, 102, 11, 104, 235, 444, 10, 272, 2, 140, 371, 345, 40, 560, 420, 68, 80, 312, 437, 571,
            65, 368, 138, 38, 185, 441, 574, 587, 31, 455, 328, 226, 384, 91, 424, 545, 307, 596, 498, 396, 484, 230,
            218, 440, 458, 291, 310, 208, 407, 203, 109, 281, 487, 429, 149, 301, 33, 316, 151, 126, 179, 515, 383, 225,
            50, 362, 313, 294, 137, 216, 338, 519, 241, 198, 496, 466, 349, 221, 55, 284, 445, 325, 194, 529, 108, 525,
            250, 103, 357, 200, 97, 530, 37, 590, 546, 219, 435, 341, 376, 385, 12, 412, 231, 289, 128, 361, 244, 257,
            554, 248, 25, 150, 116, 318, 121, 542, 58, 516, 105, 465, 73, 76, 504, 18, 35, 580, 541, 90, 271, 380, 130,
            26, 366, 202, 157, 133, 426, 287, 141, 335, 439, 180, 374, 409, 442, 467, 223, 564, 323, 483, 319, 247, 279,
            531, 41, 451, 197, 39, 239, 473, 317, 165, 379, 584, 62, 32, 356, 351, 188, 276, 305, 269, 30, 418, 567, 372,
            187, 111, 67, 461, 494, 588, 343, 533, 162, 212, 251, 422, 322, 217, 253, 199, 427, 391, 304, 419, 479, 224,
            57, 118, 54, 388, 117, 300, 29, 182, 177, 502, 170, 52, 406, 526, 462, 551, 557, 428, 120, 127, 555, 334,
            246, 344, 156, 129, 393, 562, 282, 369, 565, 152, 83, 550, 98, 410, 114, 450, 475, 4, 191, 566, 523, 308,
            77, 520, 22, 7, 552, 578, 506, 270, 326, 548, 454, 168, 514, 178, 196, 360, 237, 556, 236, 132, 266, 205,
            331, 581, 329, 399, 398, 252, 457, 113, 449, 493, 575, 181, 106, 381, 89, 320, 309, 595, 44, 59, 107, 390,
            569, 488, 210, 298, 559, 472, 505, 183, 358, 136, 355, 568, 512, 591, 135, 16, 69, 220, 594, 446, 280, 51,
            95, 443, 288, 96, 245, 499, 204, 348, 122, 155, 74, 278, 9, 375, 415, 190, 64, 537, 158, 397, 110, 456, 34,
            453, 330, 268, 558, 311, 47, 66, 403, 387, 561, 492, 262, 507, 501, 86, 577, 131, 161, 339, 509, 434, 573,
            539, 227, 82, 597, 6, 547, 303, 14, 232, 15, 324, 482, 365, 491, 256, 400, 169, 78, 100, 411, 432, 84, 394,
            342, 517, 145, 201, 354, 353, 489, 544, 286, 229, 593, 242, 186, 302, 175, 395, 148, 549, 543, 87, 46, 497,
            513, 3, 490, 49, 99, 93, 425, 23, 146, 585, 563, 404, 214, 94, 163, 598, 164, 123, 538, 378, 433, 534, 285,
            553, 572, 53, 92, 222, 184, 125, 389, 274, 485, 72, 382, 167, 364, 144, 464, 142, 370, 17, 42, 295, 24, 75,
            81, 386, 600, 401, 405, 70, 21, 413, 293, 101, 511, 524, 60, 174, 243, 209, 8, 56, 234, 417, 518, 340, 207,
            480, 159, 273, 13, 79, 333, 173, 495, 264, 373, 166, 20, 470, 45, 192, 408, 579, 500, 589, 527, 36, 583, 592,
            290, 176, 508, 476, 63, 477, 160, 359, 528, 521, 296, 448, 392, 447, 540, 85, 522, 228, 153, 570, 154, 261,
            576, 431, 195, 249, 240, 206, 478, 460, 48, 503, 189, 61, 430, 115, 321, 299, 112, 265, 468, 436, 459, 71,
            347, 481, 27, 438, 586, 275, 314, 260, 283, 414, 233, 211, 263, 367, 5, 292, 510, 346, 259, 486, 267, 582,
            599, 172, 297, 350, 336, 352, 238, 416, 28, 193, 171, 306, 337, 143, 139, 421, 258, 277, 255]


def min_hash(user_businesses, a_argument_list, hash_function):
    # [b1, b2, b3, ...]
    business_list = user_businesses[1]
    signature_matrix_column = []
    # execute outer for loop n times, compute signature_matrix[cur_user]
    for i in range(len(a_argument_list)):
        tmp = 9999999
        # execute inner for loop len(business_list) times, compute signature_matrix[cur_user][cur_hash_function]
        for business_index in business_list:
            tmp = min(hash_function(a_argument_list[i], business_index), tmp)
        signature_matrix_column.append(tmp)
    # return (cur_user, signature_matrix[cur_user])
    return user_businesses[0], signature_matrix_column


def LSH(signature_matrix_column, row, band):
    # n = 4, row = 2, band = 2
    # signature_matrix_column = (u, [h1, h2, h3, h4])
    # result                  = [((band1, sum(h1, h2)), [u]), ((band2, sum(h3, h4)), [u])]
    result = []
    user_index = signature_matrix_column[0]
    hash_values = signature_matrix_column[1]
    row_index = 0
    for band_index in range(band):
        one_band_hash_values = hash_values[row_index:row_index + row]
        row_index += row
        result.append(((band_index, sum(one_band_hash_values)), [user_index]))
    return result


def jaccard_similarity(pair, user_businesses_dict):
    user_set1 = set(user_businesses_dict[pair[0]])
    user_set2 = set(user_businesses_dict[pair[1]])
    similarity = float(len(user_set1.intersection(user_set2)) / len(user_set1.union(user_set2)))
    return pair, similarity


def user_based_Pearson_similarity(user_pairs, user_business_star_dict):
    # [(business1, star1_1), (business2, star1_2), (business3, star1_3), ...]
    u1_business_star = user_business_star_dict[user_pairs[0]]
    # [(business1, star2_1), (business2, star2_2), (business4, star2_4), ...]
    u2_business_star = user_business_star_dict[user_pairs[1]]

    u1_set = set()
    u2_set = set()
    u1_dict = dict()
    u2_dict = dict()

    for b1, s1 in u1_business_star:
        u1_set.add(b1)
        if b1 not in u1_dict:
            u1_dict[b1] = [s1]
        else:
            u1_dict[b1].append(s1)
    for b2, s2 in u2_business_star:
        u2_set.add(b2)
        if b2 not in u2_dict:
            u2_dict[b2] = [s2]
        else:
            u2_dict[b2].append(s2)

    co_ratings_set = u1_set.intersection(u2_set)

    # at least 3 co-rated businesses
    if len(co_ratings_set) < 3:
        return user_pairs[0], user_pairs[1], 0
    else:
        # co_ratings_stars : [(star1_1, star2_1), (star1_2, star2_2), (star1_3, star2_3), ...]
        co_ratings_stars = []
        for business_id in co_ratings_set:
            star_one = sum(u1_dict[business_id]) / len(u1_dict[business_id])
            star_two = sum(u2_dict[business_id]) / len(u2_dict[business_id])
            co_ratings_stars.append((star_one, star_two))
        return user_pairs[0], user_pairs[1], co_ratings_Pearson_similarity(co_ratings_stars)


if __name__ == '__main__':
    start_time = time()

    # dataset/train_review.json output/task3item.model item_based
    # dataset/train_review.json output/task3user.model user_based
    train_file = sys.argv[1]
    model_file = sys.argv[2]
    cf_type = sys.argv[3]

    conf = SparkConf().set("spark.executor.memory", "4g").set("spark.driver.memory", "4g")
    sc = SparkContext(appName="Task2_Train", conf=conf)

    input_rdd = sc.textFile(train_file) \
        .map(lambda x: json.loads(x)) \
        .map(lambda x: (x["business_id"], x["user_id"], x["stars"]))

    business_index_rdd = input_rdd.map(lambda x: x[0]).distinct().zipWithIndex()
    business_index_dict = business_index_rdd.collectAsMap()
    index_business_dict = business_index_rdd.map(lambda x: (x[1], x[0])).collectAsMap()

    user_index_rdd = input_rdd.map(lambda x: x[1]).distinct().zipWithIndex()
    user_index_dict = user_index_rdd.collectAsMap()
    index_user_dict = user_index_rdd.map(lambda x: (x[1], x[0])).collectAsMap()

    if cf_type == "item_based":
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

    elif cf_type == "user_based":
        # (u1, [b1, b2, b3, ...])
        user_businesses_rdd = input_rdd.map(lambda x: (user_index_dict[x[1]], [business_index_dict[x[0]]])) \
            .reduceByKey(add).map(lambda x: (x[0], list(set(x[1]))))
        # {u1 : [b1, b2, b3, ...]}
        user_businesses_dict = user_businesses_rdd.collectAsMap()

        # Step 1. using Min-Hash & LSH to find the similar user pairs, same as task1

        n = 600
        row = 2
        band = int(n / row)
        a_argument_list = generate_argument_a()
        b = 233333
        p = 2333333
        # m = 10253
        m = business_index_rdd.count()

        def hash_function(argument_b, argument_p, argument_m):
            def hash_func(argument_a, argument_x):
                return ((argument_a * argument_x * 17 + argument_b) % argument_p) % argument_m
            return hash_func

        hash_function = hash_function(b, p, m)

        # min_hash : compute and get signature matrix
        # (u, [b1, b2, b3, ...]) => (u, [h1(u), h2(u), h3(u), ...])
        signature_matrix_rdd = user_businesses_rdd.map(lambda x: min_hash(x, a_argument_list, hash_function))

        # apply LSH algorithm
        # (u, [h1, h2, h3, h4]) => [((band1, sum(h1, h2)), [u]), ((band2, sum(h3, h4)), [u])]
        split_signature_matrix_rdd = signature_matrix_rdd.flatMap(lambda x: LSH(x, row, band))

        # Step 1. find all candidate pairs
        #     ((band1, sum(h1, h2)), [u1]) + ((band1, sum(h1, h2)), [u2])
        #     => ((band1, sum(h1, h2)), [u1, u2])
        #     => [u1, u2]
        # Step 2. compute the similarity of all candidate pairs
        #     [u1, u2] => ([u1, u2], similarity)
        # Step 3. filter all final user pairs with similarity >= 0.01
        similar_user_pairs = split_signature_matrix_rdd.reduceByKey(lambda x, y: x + y) \
            .map(lambda x: x[1]) \
            .filter(lambda x: len(x) > 1) \
            .flatMap(lambda x: combinations(sorted(x), 2)) \
            .distinct() \
            .map(lambda x: jaccard_similarity(x, user_businesses_dict)) \
            .filter(lambda x: x[1] >= 0.01) \
            .map(lambda x: x[0])

        # Step 2. compute the Pearson correlation of every user pair, same as task3 case1

        # (u, [(b, s)]) => (u, [(b1, s1), (b2, s2), ...]) => {u : [(b1, s1), (b2, s2), ...]}
        user_business_star_dict = input_rdd.map(lambda x: (user_index_dict[x[1]], [(business_index_dict[x[0]], x[2])])) \
            .reduceByKey(add).collectAsMap()

        # [u1, u2] => (u1, u2, sim)
        user_pairs = similar_user_pairs.map(lambda x: user_based_Pearson_similarity(x, user_business_star_dict)) \
            .filter(lambda x: x[2] > 0) \
            .map(lambda x: (index_user_dict[x[0]], index_user_dict[x[1]], x[2])) \
            .collect()

        with open(model_file, "w+") as output:
            for index, elem in enumerate(user_pairs):
                if index == len(user_pairs) - 1:
                    output.write(json.dumps({'u1': elem[0], 'u2': elem[1], 'sim': elem[2]}))
                else:
                    output.write(json.dumps({'u1': elem[0], 'u2': elem[1], 'sim': elem[2]}) + "\n")
        output.close()

    end_time = time()

    print('Duration: %.2f' % (end_time - start_time))
