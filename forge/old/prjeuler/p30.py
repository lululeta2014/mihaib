# Find the sum of all the numbers that can be written
# as the sum of fifth powers of their digits.

# 7 * 9 ** 5 has 6 digits -> 7 digit numbers can't be written in this way

from prjeuler import get_base_10_digits

def sum_fifth_power_of_digits(n):
    L = get_base_10_digits(n)
    s = 0
    for d in L:
        s += d ** 5
    return s

if __name__ == '__main__':
    upper_limit = 10 ** 6
    s = 0
    for i in range(2, upper_limit):
        if i == sum_fifth_power_of_digits(i):
            s += i
    print(s)
