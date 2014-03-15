from prjeuler import isPrime

if __name__ == '__main__':
    size = 1
    n = 1
    primes = 0
    primes_10 = primes * 10
    diag_elems = size * 2 - 1
    while size == 1 or primes_10 >= diag_elems:
        size += 2
        diag_elems += 4
        for i in range(4):
            n += size - 1
            if isPrime(n):
                primes += 1
                primes_10 += 10
    print(size)
