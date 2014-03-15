import math
import fractions

if __name__ == '__main__':
    limit = 1500000
    half_limit = limit // 2
    sqrt_limit = int(math.sqrt(limit))
    solutions = [0 for i in range(limit + 1)]

    # to generate all primitive triplets
    # gcd(a, b) == 1 and exactly one of a and b is even
    # generate all multiples for each primitive triplet
    for a in range(2, sqrt_limit + 1):
        for b in range(1, a):
            if (fractions.gcd(a, b) == 1) and ((a + b) % 2 == 1):
                p1 = a*a + b*b
                #if p1 >= half_limit:
                 #   break
                p2 = a*a - b*b
                p3 = 2*a*b
                perim = p1+p2+p3
                for p in range(perim, limit + 1, perim):
                    solutions[p] += 1

    print(solutions.count(1))
