# Find the first four consecutive integers to have four distinct primes factors

def has_exactly_4_prime_factors(n):
    if n < 2:
        return False

    count = 0
    f = 2
    if n % f == 0:
        count += 1
        while n % f == 0:
            n //= f

    f = 3
    while f <= n:
        if n % f == 0:
            count += 1
            if count > 4:
                return False
            while n % f == 0:
                n //= f
        f += 2

    return count == 4

if __name__ == '__main__':
    i = 1
    while True:
        i += 1
        print(i)

        if not has_exactly_4_prime_factors(i):
            continue

        if not has_exactly_4_prime_factors(i + 1):
            i += 1
            continue

        if not has_exactly_4_prime_factors(i + 2):
            i += 2
            continue

        if not has_exactly_4_prime_factors(i + 3):
            i += 3
            continue
        break

    print(i)
