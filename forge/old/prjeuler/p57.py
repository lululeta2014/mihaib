from fractions import Fraction

if __name__ == '__main__':
    f = Fraction(1)
    count = 0
    for i in range(1000):
        f = 1 + Fraction(1, 1 + f)
        n = f.numerator
        d = f.denominator
        while d != 0:
            n //= 10
            d //= 10
        if n > 0:
            count += 1
    print(count)
