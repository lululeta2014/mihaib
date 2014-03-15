# What is the sum of the digits of the number 2^(1000)?

from prjeuler import get_base_10_digits

if __name__ == '__main__':
    digits = get_base_10_digits(2 ** 1000)
    s = 0
    for d in digits:
        s += d
    print(s)
