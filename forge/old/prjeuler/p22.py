# What is the total of all the name scores in the file?
# For example, when the list is sorted into alphabetical order,
# COLIN, which is worth 3 + 15 + 12 + 9 + 14 = 53,
# is the 938th name in the list.
# So, COLIN would obtain a score of 938 × 53 = 49714.

from prjeuler import word_value

if __name__ == '__main__':
    with open('p22-names.txt', 'r') as f:
        lines = f.readlines()

    L = []
    for line in lines:
        L.extend(line.split(","))
    L.sort()

    s = sum([(i + 1) * word_value(name) for i, name in enumerate(L)])
    print(s)
