# What is the smallest number divisible by each of the numbers 1 to 20?

from prjeuler import prime_factors

if __name__ == '__main__':
    factors = {}
    for n in range(1, 21):
        for (f,p) in prime_factors(n).items():
            if not f in factors:
                factors[f] = p
            else:
                factors[f] = max(factors[f], p)
    # build the solution
    n = 1
    for (f,p) in factors.items():
        n *= f ** p
    print(n)
