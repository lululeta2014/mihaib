# Find the smallest positive integer, x,
# such that 2x, 3x, 4x, 5x, and 6x, contain the same digits.

from prjeuler import get_base_10_digits

if __name__ == '__main__':
    x = 1
    while True:
        x += 1
        digits = sorted(get_base_10_digits(x))

        if sorted(get_base_10_digits(2 * x)) != digits:
            continue

        if sorted(get_base_10_digits(3 * x)) != digits:
            continue

        if sorted(get_base_10_digits(4 * x)) != digits:
            continue

        if sorted(get_base_10_digits(5 * x)) != digits:
            continue

        if sorted(get_base_10_digits(6 * x)) != digits:
            continue

        break

    print(x)
