from fractions import Fraction

if __name__ == '__main__':
    max_d = 1000000
    solution = Fraction(2, 5)
    solution = 0
    upper_limit = Fraction(3, 7)

    for d in range(1, max_d + 1):
        min_n = int(solution * d)
        for n in range(min_n, d):
            candidate = Fraction(n, d)
            if candidate >= upper_limit:
                break
            if candidate > solution:
                solution = candidate
    print(solution)
