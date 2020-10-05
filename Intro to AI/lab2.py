###
# Justin Gonzales, 331 LAB1
# Classification - Given 15 word snippets, determine if they are either English or Dutch. (Decision trees and adaboost)
# Due 2/23/2020
###
import sys
import math
import random
import pickle

K = 300     # Number of decision stumps (Hypotheses K)
MAX_TREE_DEPTH = 6     # 1 - Max tree depth for a decision tree
BASE_TREE_DEPTH = 2    # Lowest depth a tree can have without pruning.


# Parses command line arguments to train a hypothesis, predict based on a hypothesis, or test a hypothesis' error rate.
def main():
    # Get arguments
    if len(sys.argv) < 2:
        print("Invalid number of command line arguments. Use commands as outlined in Writeup.txt. Exiting.")
        exit()
    if sys.argv[1] == "predict":
        # predict function
        hypoth = sys.argv[2]
        file_name = sys.argv[3]
        predict(hypoth, file_name)
    elif sys.argv[1] == "train":
        # train function
        train_data = sys.argv[2]
        file_name_out = sys.argv[3]
        ltype = sys.argv[4]
        if ltype == "dt":
            train_dt(train_data, file_name_out)
        elif ltype == "ada":
            train_ada(train_data, file_name_out)
        else:
            print("Invalid training type - Exiting.")
            exit()
    elif sys.argv[1] == "test":
        # testing error rate given example data.
        hypoth = sys.argv[2]
        file_name = sys.argv[3]
        test_suite(hypoth, file_name)
    else:
        print("Invalid command line argument - Exiting.")
        exit()

    # Arguments successfully parsed.


# Representation of a decision tree. Branches to next attr or classification are values in dict, from T/F keys
class DecisionTree:
    def __init__(self, root_test, root_dict):
        self.root_test = root_test
        self.root_dict = root_dict


# -------------------------------------------------------------------------------------------


# Function directing high level training of decision tree - returns serialized hypothesis
def train_dt(data, file_name):
    data_dict = parse_examples(data)  # Place examples in a string: lang dict
    print("Example data read in.")
    pos_vals = 0                       # 'Positive' Values where the 'goal' is english.
    neg_vals = 0                       # 'Negative' Values is then Dutch.
    for values in data_dict.values():
        if values == "en":
            pos_vals += 1
        elif values == "nl":
            neg_vals += 1
        else:
            print("Invalid language training data detected - Exiting")
            exit()
    attr = determine_attr(data_dict)  # attr are determined based on example training data
    print("Attributes of examples determined.")
    tree = dt_learning(data_dict, attr, None, pos_vals, neg_vals, 0)  # full new tree is created
    print("Decision Tree created.")
    # prunes tree
    #print("Tree pruned.")
    hypoth_out = open(file_name, "wb")
    pickle.dump(tree, hypoth_out)  # serializes tree and write to file.
    print("Tree serialized.")
    print("Written serialization to file: " + file_name + ".")
    hypoth_out.close()

    print("Done.")


# The decision tree learning algorithm implementation
def dt_learning(examples, attr, parents, pos_vals, neg_vals, depth):
    if len(examples) == 0:
        return plurality(parents)
    elif same_class(examples):
        return list(examples.values())[0]
    elif len(attr) == 0:
        return plurality(examples)
    else:
        temp_imp = 0

        for a in attr:  # determines A
            temp_a = a
            challenge_imp = importance(examples, a, pos_vals, neg_vals)
            if challenge_imp > temp_imp:
                temp_imp = challenge_imp
                temp_a = a

        tree = DecisionTree(temp_a, dict())  # creates a new tree with a root attr and the 'branches' to the next

        if depth > MAX_TREE_DEPTH:
            return plurality(examples)

        for i in range(0, 2):
            if i == 0:
                val = True
            if i == 1:
                val = False
            exs = dict()
            for exam in examples.keys():
                if temp_a[exam] == val:
                    exs[exam] = examples[exam]  # explores the true or false side of tree, passing only those on.
            if attr.__contains__(temp_a):
                attr.remove(temp_a)                     # removes most stringent attr from list
            subtree = dt_learning(exs, attr, examples, pos_vals, neg_vals, depth + 1)  # creates a subtree from attr
            root = tree.root_dict
            root[val] = subtree  # tree branches to that subtree from curr val
            tree.root_dict = root
    return tree


# helper function for dt_learning - returns plurality value of an example
def plurality(examples):
    pos_vals = 0
    neg_vals = 0
    for e in examples.values():
        if e:
            pos_vals += 1
        else:
            neg_vals += 1

    if pos_vals > neg_vals:
        return "en"
    elif neg_vals > pos_vals:
        return "nl"
    else:
        rand = random.randint(0, 1)
        if rand == 1:
            return "nl"
        else:
            return "en"


# helper function for dt_learning - returns importance of an attribute attr given the examples results
def importance(examples, attr, pos_vals, neg_vals):
    # implements information gain algorithm to determine entropy reduction (importance)
    # uses remainder 1 and 2 instead of d distinct values as all values are boolean yes/no

    attr_pos1 = 0
    attr_pos2 = 0
    attr_neg1 = 0
    attr_neg2 = 0
    for keys in attr.keys():
        val = attr[keys]
        if examples.keys().__contains__(keys):
            result = examples[keys]
        else:
            continue
        if result == "en":
            if val:
                attr_pos1 += 1  # pk values - Positive True
            elif not val:
                attr_pos2 += 1  # pk+1 values - Positive False
        else:
            if val:
                attr_neg1 += 1  # nk values - Negative True
            elif not val:
                attr_neg2 += 1  # nk+1 values - Negative False
    if attr_pos1 == 0 and attr_neg1 == 0:       # In case of a bad attribute or very skewed test set entry
        remainder1 = 0
        remainder2 = ((attr_pos2 + attr_neg2) / (pos_vals + neg_vals)) * get_b(attr_pos2, attr_neg2)
    elif attr_pos2 == 0 and attr_neg2 == 0:
        remainder2 = 0
        remainder1 = ((attr_pos1 + attr_neg1) / (pos_vals + neg_vals)) * get_b(attr_pos1, attr_neg1)
    else:
        remainder1 = ((attr_pos1 + attr_neg1) / (pos_vals + neg_vals)) * get_b(attr_pos1, attr_neg1)
        remainder2 = ((attr_pos2 + attr_neg2) / (pos_vals + neg_vals)) * get_b(attr_pos2, attr_neg2)

    remainder = remainder1 + remainder2
    b = get_b(pos_vals, neg_vals)
    return b - remainder


# helper function for importance - returns the entropy of a boolean random variable based on # of pos and neg values
def get_b(p, n):
    q = p / (p + n)
    if q == 0.0:
        return 0
    elif q == 1:
        return 0
    b = ((q * math.log2(q)) + ((1 - q) * math.log2((1-q)))) * -1
    return b


# helper function for dt_learning - returns true if all examples have same classification, otherwise false
def same_class(examples):
    temp_vals = list(examples.values())[0]
    for vals in examples.values():
        if vals == temp_vals:
            continue
        else:
            return False
    return True


# -----------------------------------------------------------------------------------------

# Function directing high level training of adaboost - returns a serialized hypothesis
def train_ada(data, file_name):
    data_dict = parse_examples(data)  # Place examples in a string: lang dict
    pos_vals = 0  # 'Positive' Values where the 'goal' is english.
    neg_vals = 0  # 'Negative' Values is then Dutch.
    for values in data_dict.values():
        if values == "en":
            pos_vals += 1
        elif values == "nl":
            neg_vals += 1
        else:
            print("Invalid language training data detected - Exiting")
            exit()
    attr = determine_attr(data_dict)  # attr are determined based on example training data

    tree = adaboost(data_dict, attr) # full new tree is created

    print("Adaboost complete.")
    hypoth_out = open(file_name, "wb")
    pickle.dump(tree, hypoth_out)  # serializes tree and writes it to file
    print("Hypothesis set serialized.")
    print("Written serialization to file: " + file_name + ".")
    hypoth_out.close()

    print("Done.")


# adaboost boosting function running the adaboost algorithm
# A hypothesis is represented by an order in which to apply attributes 1-10 to reach a decision.
def adaboost(examples, attr):
    N = len(attr)
    w = list()  # N example weights
    h = [None] * K  # K hypotheses
    z = [None] * K  # K hypothesis weights
    for i in range(0, N):
        num = 1 / N
        w.append(num)         # set all w to 1/N to start.
    for k in range(1, K):
        h.append(0)
        z.append(0)
        h[k] = learning_func_weights(examples, attr, None, w, 0)
        error = 0
        for j in range(1, N):
            if get_class(h[k], list(examples.keys())[j]) != list(examples.values())[j]:
                error += w[j]
        for j in range(1, N):
            if get_class(h[k], list(examples.keys())[j]) == list(examples.values())[j]:
                w[j] *= (error / (1 - error))
        w = normalize(w)
        if error == 0:
            z[k] = 0
        else:
            z[k] = math.log10((1 - error))/error
    return weighted_majority(h, z)


# 'learning algorithm' for adaboost - creates a valid decision tree based on input weights.
def learning_func_weights(examples, attr, parents, w, depth):
    if len(examples) == 0:
        return plurality(parents)
    elif same_class(examples):
        return list(examples.values())[0]
    elif len(attr) == 0:
        return plurality(examples)
    else:
        temp_imp = 0
        temp_a = attr[0]
        for i in range(0, len(attr)):  # determines A
            a = attr[i]
            challenge_imp = w[i]
            if challenge_imp > temp_imp:
                temp_imp = challenge_imp
                temp_a = a

        tree = DecisionTree(temp_a, dict())  # creates a new tree with a root attr and the 'branches' to the next

        if depth > MAX_TREE_DEPTH:
            return tree

        for val in temp_a.values():
            exs = dict()
            for exam in examples.keys():
                if temp_a[exam] == val:
                    exs[exam] = examples[exam]  # explores the true or false side of tree by passing on those
            if attr.__contains__(temp_a):
                attr.remove(temp_a)                    # removes most stringent attr from list
            subtree = learning_func_weights(exs, attr, examples, w, depth + 1)  # creates a subtree from attr
            root = tree.root_dict
            root[val] = subtree  # tree branches to that subtree from curr val
            tree.root_dict = root
    return tree


# steps through a created tree given an input to produce it's classification. Helper for adaboost.
def get_class(tree, key):
    if tree == "nl":
        return "nl"
    elif tree == "en":
        return "en"
    else:
        root_attr = tree.root_test
        root_dict = tree.root_dict
        while True:
            val = root_attr[key]
            new_tree = root_dict[val]
            if new_tree == "nl":
                return "nl"
            elif new_tree == "en":
                return "en"
            else:
                root_attr = new_tree.root_test
                root_dict = new_tree.root_dict


# normalization function for weights
def normalize(w):
    summ = 0
    count = 0
    for weights in w:
        summ += weights
        count += 1
    for weights in w:
        weights /= summ
    return w


# returns the weighted majority hypothesis based on hypothesis weights voting
def weighted_majority(h, z):
    challenge_weight = 0
    challenge = h[1]
    for i in range(0, len(h)):
        weight = z[i]
        hypoth = h[i]
        if h[i] is None or z[i] is None:
            break
        if weight > challenge_weight:
            challenge_weight = weight
            challenge = hypoth

    return challenge
# --------------------------------------------------------------------------------------------


# use delimiter '|' to break up into value and answer data, returns example data as dict.
def parse_examples(data):
    examples = dict()
    enc = 'utf-8'
    data_file = open(data, "r", encoding=enc)
    d = data_file.read().splitlines()
    data_file.close()
    for lines in d:
        lines.strip("().,?!/&%$#@^*-_=+[]{}`~\'\\'\"")  # strips punctuation.
        l = lines.split("|")
        examples[l[1]] = l[0]   # creates a dict of each line to it's nl or en value.
    return examples


# Read in a file of text for predictions.
def parse_file(data):
    enc = 'utf-8'
    data_file = open(data, "r", encoding=enc)
    d = data_file.read().splitlines()
    data_file.close()
    for lines in d:
        lines.strip('().,?!/&%$#@^*-_=+[]{}`~\'\\')  # strips punctuation.
    return d


# Creates a dictionary mapping an example line to its binary yes/no value based on whether it has a given hardcoded
# attribute. These are compiled and returned as a list of attributes 'attr'.
def determine_attr(examples):
    attr = list()

    attr1 = dict()  # Whether or not the string contains the string ' de '.
    for key in examples:
        if key.lower().find(' de ') == -1:
            attr1[key] = False
        else:
            attr1[key] = True
    attr1["?"] = 1
    attr.append(attr1)

    attr2 = dict()  # Whether or not the string contains the string ' het '.
    for key in examples:
        if key.lower().find(' het ') == -1:
            attr2[key] = False
        else:
            attr2[key] = True
    attr2["?"] = 2
    attr.append(attr2)

    attr3 = dict()  # Whether or not the string contains the string ' the '.
    for key in examples:
        if key.lower().find(' the ') == -1:
            attr3[key] = False
        else:
            attr3[key] = True
    attr3["?"] = 3
    attr.append(attr3)

    attr4 = dict()  # Whether or not the string contains the string 'a'.
    for key in examples:
        if key.lower().find(' a ') == -1:
            attr4[key] = False
        else:
            attr4[key] = True
    attr4["?"] = 4
    attr.append(attr4)

    attr5 = dict()  # Whether or not the string contains 15 or more 'e' characters.
    for key in examples:
        if key.lower().count('e') < 15:
            attr5[key] = False
        else:
            attr5[key] = True
    attr5["?"] = 5
    attr.append(attr5)

    attr6 = dict()  # Whether or not the string contains 2 or more 'v' characters.
    for key in examples:
        if key.lower().count('v') < 2:
            attr6[key] = False
        else:
            attr6[key] = True
    attr6["?"] = 6
    attr.append(attr6)

    attr7 = dict()  # Whether or not the string contains any 'z' characters.
    for key in examples:
        if key.lower().count('z') < 1:
            attr7[key] = False
        else:
            attr7[key] = True
    attr7["?"] = 7
    attr.append(attr7)

    attr8 = dict()  # Whether or not the string's avg word length is greater than 5.5.
    for key in examples:
        temp = key
        temp.split(' ')
        num_words = len(temp)
        char_count = 0
        for words in temp:
            for letters in words:
                char_count += 1
        avg_word_len = float(char_count / num_words)
        if avg_word_len < 5.5:
            attr8[key] = False
        else:
            attr8[key] = True
    attr8["?"] = 8
    attr.append(attr8)

    attr9 = dict()  # Whether or not the string contains more than one 12 letter word.
    for key in examples:
        temp = key
        temp.split(' ')
        big_words_count = 0
        for words in temp:
            char_count = 0
            for letters in words:
                char_count += 1
            if char_count >= 12:
                big_words_count += 1
        if big_words_count < 2:
            attr9[key] = False
        else:
            attr9[key] = True
    attr9["?"] = 9
    attr.append(attr9)

    attr10 = dict()  # Whether or not the string contains the string 'aa'.
    for key in examples:
        if key.lower().find('aa') == -1:
            attr10[key] = False
        else:
            attr10[key] = True
    attr10["?"] = 10
    attr.append(attr10)
    # hopefully not a lot of aardvarks or bazaars in english.

    attr11 = dict()  # Whether or not the string contains the string 'ij'.
    for key in examples:
        if key.lower().find('ij') == -1:
            attr11[key] = True
        else:
            attr11[key] = False
    attr11["?"] = 11
    attr.append(attr11)

    attr12 = dict()  # Whether or not the string contains the string 'th'.
    for key in examples:
        if key.lower().find('th') == -1:
            attr12[key] = False
        else:
            attr12[key] = True
    attr12["?"] = 12
    attr.append(attr12)

    attr13 = dict()  # Whether or not the string contains more 't' than 'n' characters.
    for key in examples:
        if key.lower().count('t') > key.lower().count('n'):
            attr13[key] = True
        else:
            attr13[key] = False
    attr13["?"] = 13
    attr.append(attr13)

    return attr
# --------------------------------------------------------------------------------------------


# Function directing high level classification using input hypothesis.
def predict(hypoth, file_name):
    data = parse_file(file_name)
    print("Data successfully read in.")

    hypoth_file = open(hypoth, "rb")
    hypoth_in = pickle.load(hypoth_file)
    print("Hypothesis deserialized.")
    hypoth_file.close()

    run_hypothesis(hypoth_in, data)
    print("Done.")


# Determines and output predicted language from a given set of data and hypothesis.
def run_hypothesis(hypoth, data):
    attr = determine_attr(data)
    tree = hypoth.root_dict
    root_test = hypoth.root_test
    for el in data:                     # For each 15 word string
        attr_num = root_test["?"]
        val = attr[attr_num-1][el]             # Get binary value from next most important attribute
        step = tree[val]                # Move to the result of that value from the tree, a subtree or prediction.
        while step != "en" and step != "nl":
            temp = step.root_test
            attr_num = temp["?"]
            val = attr[attr_num-1][el]
            step = step.root_dict[val]            # Continue progressing in the same fashion until prediction reached
        if step == "en":                # Print the prediction for the sentence.
            print("en")
        elif step == "nl":
            print("nl")


# Gives error rate for a given hypothesis and data, given the answers.
def test_suite(hypoth, file_name):
    examples = parse_examples(file_name)
    print("Data successfully read in.")

    hypoth_file = open(hypoth, "rb")
    hypoth_in = pickle.load(hypoth_file)
    print("Hypothesis deserialized.")
    hypoth_file.close()

    test_hypothesis(hypoth_in, examples, file_name)
    print("Done.")


# Determines and output predicted language from a given set of data and hypothesis.
def test_hypothesis(hypoth, data, file_name):
    correct_vals = 0
    incorrect_vals = 0                  # Counters for # of correct and incorrect predictions
    attr = determine_attr(data)
    tree = hypoth.root_dict
    root_test = hypoth.root_test
    for el in data:  # For each 15 word string
        attr_num = root_test["?"]
        val = attr[attr_num - 1][el]  # Get binary value from next most important attribute
        step = tree[val]  # Move to the result of that value from the tree, a subtree or prediction.
        while step != "en" and step != "nl":
            temp = step.root_test
            attr_num = temp["?"]
            val = attr[attr_num - 1][el]
            step = step.root_dict[val]  # Continue progressing in the same fashion until prediction reached
        if step == "en":  # Count if prediction was correct.
            if data[el] == "en":
                correct_vals += 1
            else:
                incorrect_vals += 1
        elif step == "nl":
            if data[el] == "nl":
                correct_vals += 1
            else:
                incorrect_vals += 1
    num_test = correct_vals + incorrect_vals
    error_rate = float((correct_vals - num_test) / num_test) * 100
    if error_rate < 0:
        error_rate *= -1
    print("The given hypothesis file has an error rate of: " + str(error_rate) + "% for " + file_name + ".")


if __name__ == "__main__":
    main()
