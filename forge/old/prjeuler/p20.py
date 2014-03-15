# Find the sum of the digits in the number 100!

from prjeuler import get_base_10_digits

if __name__ == '__main__':
    n = 1
    for i in range(1, 101):
        n *= i
    print(sum(get_base_10_digits(n)))
