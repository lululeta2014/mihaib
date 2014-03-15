# using Euler's totient and the pdf from problem 69,
# we're trying to find n such that phi(n)/(n-1) < 15499/94744

# I don't know if I've got this right, but I'm trying to produce
# n by multiplying consecutive prime numbers.
# So phi(n)/(n-1) = n/(n-1) * Product((p-1)/p)

from fractions import Fraction

if __name__ == '__main__':
    prime_numbers = []
    target = Fraction(15499, 94744)
    best_found = Fraction(4, 10)
    n = 1
    p = 1
    p_product = 1

    while best_found >= target:
        # find next prime
        prime = False
        while not prime:
            p += 1
            prime = True
            for f in prime_numbers:
                if f * f > p:
                    break
                if p % f == 0:
                    prime = False
                    break

        prime_numbers.append(p)
        n *= p
        p_product *= Fraction(p-1, p)

        crt_resilience = p_product * Fraction(n, n - 1)
        if crt_resilience < best_found:
            best_found = crt_resilience
            print(best_found)

    print(n, crt_resilience, float(crt_resilience))

    # the pseudo-strategy is to hunt-down the number, because
    # the one we find is not the smallest
    # this `hacking by hand' goes back 1 factor and starts adding it to itself
    p_product /= Fraction(p-1, p)
    n //= p
    crt_resilience = p_product * Fraction(n, n-1)
    step = n
    while crt_resilience >= target:
        n += step
        crt_resilience = p_product * Fraction(n, n-1)

    print(n, crt_resilience, float(crt_resilience))
