from prjeuler import isPrime

if __name__ == '__main__':
    max_n = 0
    product = 0
    for a in range(-999, 1000):
        for b in range(-999, 1000):
            n = 0
            while isPrime(n * (n + a) + b):
                n += 1
            if n > max_n:
                max_n = n
                product = a * b
    print(product)
