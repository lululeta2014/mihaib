# Find the maximum total from top to bottom of the triangle below:

data_str = "\
75 \
95 64 \
17 47 82 \
18 35 87 10 \
20 04 82 47 65 \
19 01 23 75 03 34 \
88 02 77 73 07 63 67 \
99 65 04 28 06 16 70 92 \
41 41 26 56 83 40 80 70 33 \
41 48 72 33 47 32 37 16 94 29 \
53 71 44 65 25 43 91 52 97 51 14 \
70 11 33 28 77 73 17 78 39 68 17 57 \
91 71 52 38 17 14 91 43 58 50 27 29 48 \
63 66 04 68 89 53 67 30 73 16 69 87 40 31 \
04 62 98 27 23 09 70 98 73 93 38 53 60 04 23"


def max_sum(L):
    if len(L) == 0:
        return 0
    n = 1
    sums = L[:n]
    L = L[n:]
    
    while L:
        n += 1
        if n > len(L):
            raise ValueError("The list is not a triangle")
        row = L[:n]
        L = L[n:]
        sums = [row[0] + sums[0]]\
            + [max(v + sums[i], v + sums[i + 1])
               for i, v in enumerate(row[1:-1])]\
            + [row[-1] + sums[-1]]

    return max(sums)

if __name__ == '__main__':
    L = [int(nr) for nr in data_str.split(" ")]
    print(max_sum(L))
