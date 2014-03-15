# Find the sum of all numbers which are equal
# to the sum of the factorial of their digits.

# Observation: 8 * 9! has 7 digits, so check numbers with up to 6 digits

from prjeuler import get_base_10_digits
import math

if __name__ == '__main__':
    s = 0
    for n in range(3, 10 ** 6):
        if n == sum([math.factorial(d) for d in get_base_10_digits(n)]):
            s += n
    print(s)
