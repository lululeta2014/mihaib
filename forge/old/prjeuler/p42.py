# how many are triangle words?
# The n^(th) term of the sequence of triangle numbers is given by
# t_(n) = ½n(n+1); so the first ten triangle numbers are:
# 1, 3, 6, 10, 15, 21, 28, 36, 45, 55
# For example, the word value for SKY is 19 + 11 + 25 = 55 = t_(10)

from prjeuler import word_value

if __name__ == '__main__':
    with open('p42-words.txt', 'r') as f:
        lines = f.readlines()

    # split lines from file
    L = []
    for line in lines:
        L.extend(line.split(','))

    # initialize the set of triangle numbers
    n = 1
    tmax = 1
    t = {1}

    # count triangle words
    triangle_words = 0
    for word in L:
        val = word_value(word)
        while val > tmax:
            n += 1
            tmax += n
            t.add(tmax)
        if val in t:
            triangle_words += 1

    print(triangle_words)
