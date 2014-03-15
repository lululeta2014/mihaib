# What is the value of the first triangle number to have over 500 divisors?
# The n(th) triangle number is 1 + 2 + 3 + ... + n

# TODO measure running time and see what gains are possible
# by using a table of primes (for the prime_factors() method).
# For instance, this table could be updated
# when a call returns higher prime factors than before

from prjeuler import prime_factors

def count_divisors(n):
    if n < 1:
        return 0
    divisors = 1
    for power in prime_factors(n).values():
        divisors *= power + 1
    return divisors

def count_divisors1(n):
    divisors = 0
    for i in range(1, n+1):
        if n % i == 0:
            divisors += 1
    return divisors

if __name__ == '__main__':
    target_count = 501
    first_number = 2 ** (target_count - 1)

    rank = 0
    n = 0
    divisors = 0
    max_div = 0

    while divisors < target_count:
        rank += 1
        n += rank
        print(n, end="\t")
        divisors = count_divisors(n)
        if divisors > max_div:
            max_div = divisors
        print(divisors, "\t", max_div)

    print(n, "has", divisors, "divisors")
