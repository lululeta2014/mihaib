# Use Euler's totient function and the formula:
# phi(n) = n * Product(1 - 1/p) - for each prime factor p (expanded below):
# phi(n) = n * (1 - 1/p1) * (1 - 1/p2) * ... * (1 - 1/pk)

if __name__ == '__main__':
    max_d = 1000000
    prime_factors = []
    solutions = 0
    for d in range(2, max_d + 1):
        phi = n = d

        for f in prime_factors:
            if f * f > n:
                break
            if n % f == 0:
                phi -= phi // f
                while n % f == 0:
                    n //= f

        if n == d:
            # d is prime
            prime_factors.append(d)
        if n > 1:
            phi -= phi // n

        solutions += phi

    print(solutions)
