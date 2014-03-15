# What is the sum of the numbers on the diagonals in a 1001 by 1001 spiral?
# A 5x5 spiral is below and the sum of both diagonals is 101
#21 22 23 24 25
#20  7  8  9 10
#19  6  1  2 11
#18  5  4  3 12
#17 16 15 14 13

if __name__ == '__main__':
    size = 1001
    s = n = 1
    for i in range(2, size, 2):
        for j in range(4):
            n += i
            s += n
    print(s)
