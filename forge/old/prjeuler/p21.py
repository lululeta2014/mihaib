# Evaluate the sum of all the amicable numbers under 10000.
# Let d(n) be defined as the sum of proper divisors of n.
# If d(a) = b and d(b) = a, where a ≠ b, then a and b are an amicable pair
# and each of a and b are called amicable numbers.
#
# For example, the proper divisors of 220 are
# 1, 2, 4, 5, 10, 11, 20, 22, 44, 55 and 110; therefore d(220) = 284.
# The proper divisors of 284 are 1, 2, 4, 71 and 142; so d(284) = 220.

from prjeuler import sum_of_divisors

if __name__ == '__main__':
    n_s = {n:s for n in range(2, 10000)
           for s in [sum_of_divisors(n)] if s < 10000 and s != n}
    s = 0
    for a in n_s:
        da = n_s[a]
        if da != a and da in n_s and n_s[da] == a:
            s += a
    print(s)
