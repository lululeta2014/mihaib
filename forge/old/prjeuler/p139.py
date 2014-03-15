from fractions import gcd
import math

if __name__ == '__main__':
    after_max_perim = 100 * 10 ** 6
    half_after_max = after_max_perim // 2
    sqrt_after_max = int(math.sqrt(after_max_perim))
    solutions = 0

    for n in range(1, sqrt_after_max):
        for m in range(1, n):
            if (n + m) % 2 == 0:
                continue
            if gcd(n, m) != 1:
                continue

            c = n*n + m*m
            if c > half_after_max:
                break
            a, b = sorted([n*n - m*m, 2*n*m])
            perim = a + b + c

            if c % (b - a) == 0:
                solutions += (after_max_perim - 1) // perim
    print(solutions)
