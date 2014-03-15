# Module with utilities for the problems on project euler

import math


def isPrime(n):
    '''Checks if a number is prime'''
    if n < 2:
        return False
    if n == 2:
        return True
    if n % 2 == 0:
        return False
    fMax = int(math.sqrt(n))
    for f in range(3, fMax + 1, 2):
        if n % f == 0:
            return False
    return True


def prime_factors(n):
    '''Returns a dictionary {factor:power} for this number's prime factors'''
    d = {}
    # treat 2 separately
    f = 2
    if n % f == 0:
        power = 0
        while n % f == 0:
            n //= f
            power += 1
        d[f] = power
    # look only for odd factors
    f = 3
    while f <= n:
        if n % f == 0:
            power = 0
            while n % f == 0:
                n //= f
                power += 1
            d[f] = power
        f += 2
    return d

def get_base_10_digits(n):
    '''Returns a list with the number's digits in base 10.

    Also works for negative numbers (strips the minus sign) and zero.'''
    if n < 0:
        n = -n
    if n == 0:
        return [0]

    digits = []
    while n > 0:
        digits.append(n % 10)
        n //= 10
    digits.reverse()
    return digits


A_byte = 'A'.encode('utf-8')[0]
Z_byte = 'Z'.encode('utf-8')[0]

def word_value(word):
    '''Sum of characters A..Z (1..26) after uppercasing, other chars ignored'''
    word = word.upper()
    return sum([b - A_byte + 1 for ch in word for b in [ch.encode('utf-8')[0]]
                if b >= A_byte and b <= Z_byte])


def sum_of_divisors(n):
    '''The sum of the proper divisors of n'''
    if n < 1:
        raise ValueError("n ({0}) must be >= 1".format(n))
    s = 0
    for i in range(1, n):
        if n % i == 0:
            s += i
    return s

