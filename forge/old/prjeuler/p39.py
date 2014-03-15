import math

if __name__ == '__main__':
    max_len = p = 0
    for p in range(1001):
        L = [(a, b, c) for a in range(p//3) for a_2 in [a * a]
                for b in range(a, p//2) for c in [p - a - b]
                if c > b and c * c == a_2 + b * b]
        if len(L) > max_len:
            max_len = len(L)
            p_max = p
    print(p_max)
