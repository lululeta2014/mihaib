# Find the only Pythagorean triplet for which a + b + c = 1000
# print a*b*c

import math

if __name__ == '__main__':
    sol = [(a, b, c) for a in range(1, 1000) for b in range(a, 1000)
            for s in (a*a + b*b,) for c in (int(math.sqrt(s)),)
            if s == c*c and a + b + c == 1000]
    print(sol)
    if len(sol) > 0:
        t = sol[0]
        print(t[0] * t[1] * t[2])
