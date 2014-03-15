# Calculate the sum of all the primes below two million.

from prjeuler import isPrime

if __name__ == '__main__':
    s = 0
    for n in range(2000000):
        if isPrime(n):
            s += n
    print(s)
