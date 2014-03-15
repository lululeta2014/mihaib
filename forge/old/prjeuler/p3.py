# What is the largest prime factor of the number 600851475143 ?

def largestPrimeFactor(n):
    if n < 2:
        raise ValueError('number must be >= 2')
    maxFact = f = 2
    while f <= n:
        if n % f == 0:
            maxFact = f
            while n % f == 0:
                n //= f
        f = f + 1
    return maxFact

if __name__ == '__main__':
    print(largestPrimeFactor(600851475143))
