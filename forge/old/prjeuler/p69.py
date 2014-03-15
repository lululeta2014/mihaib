after_last = 1000001
P = [0 for i in range(after_last)]
P[0] = P[1] = 0

prime_factors = []

if __name__ == '__main__':
    for n in range(2, after_last):
        x = n
        phi = x
        is_prime = True

        for f in prime_factors:
            if f * f > x:
                break
            if x % f == 0:
                is_prime = False
                phi -= phi // f
                while x % f == 0:
                    x //= f

        if is_prime:
            prime_factors.append(n)
        if x > 1:
            phi -= phi // x
        P[n] = phi

    found_n, max_result = 0, 0
    for n, v in enumerate(P[2:], 2):
        if n / v > max_result:
            found_n, max_result = n, n / v
            print(found_n, max_result)
    print(found_n, max_result)
