#! /usr/bin/env python3

from fractions import Fraction

# generator for combinations of n, k as list
def comb(n, k):
    if n <= 0 or k <= 0 or k > n:
        raise ValueError('Invalid args k', k, 'n', n, 'for comb(..)')

    v = []
    for i in range(1, k+1):
        v.append(i)

    while True:
        yield list(v)

        i = k-1
        while i >= 0 and v[i] + (k - 1 - i) >= n:
            i = i - 1

        if i < 0:
            return

        v[i] += 1
        for j in range(i+1, k):
            v[j] = v[j-1] + 1


if __name__ == '__main__':
    turns = 15
    red_chance = []
    blue_chance = []
    for i in range(1, turns+1):
        red_chance.append(Fraction(1, i+1))
        blue_chance.append(Fraction(i, i+1))

    win_chance = Fraction(0, 1)
    for k in range(turns // 2 + 1, turns + 1):
        for v in comb(turns, k):
            s = set(v)
            p = Fraction(1, 1)
            for i in range(1, turns+1):
                if i in s:
                    p *= red_chance[i-1]
                else:
                    p *= blue_chance[i-1]
            win_chance += p

    print(win_chance)
    print(int(Fraction(1, 1) / win_chance))
