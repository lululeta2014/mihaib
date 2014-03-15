# What is the 10001(st) prime number?

import math
from prjeuler import isPrime

def findNextPrime(startFrom):
    while not isPrime(startFrom):
        startFrom = startFrom + 1
    return startFrom

if __name__ == '__main__':
    # short test for isPrime
    print([n for n in range(-10, 101) if isPrime(n)])

    crtPrime = 1;
    for i in range(10001):
        crtPrime = findNextPrime(crtPrime + 1)

    print(crtPrime)
