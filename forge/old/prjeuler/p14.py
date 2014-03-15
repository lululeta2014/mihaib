# The following iterative sequence is defined for the set of positive integers:
#
# n → n/2 (n is even)
# n → 3n + 1 (n is odd)
#
# Using the rule above and starting with 13, we generate the sequence:
# 13 → 40 → 20 → 10 → 5 → 16 → 8 → 4 → 2 → 1
#
# Which starting number, under one million, produces the longest chain?
# NOTE: Once the chain starts the terms are allowed to go above one million.

def get_next(n):
    if n % 2 == 0:
        return n // 2
    return 3 * n + 1

lengths = {}

def run_sequence(start):
    n = start
    length = 1
    while n != 1:
        if n in lengths:
            length += lengths[n] - 1
            break
        n = get_next(n)
        length += 1

    lengths[start] = length

if __name__ == '__main__':
    for i in range(1, 1000001):
        run_sequence(i)

    best_number = best_length = 0
    for n,l in lengths.items():
        if l > best_length:
            best_number, best_length = n, l
    print("Longest sequence has", best_length,
            "terms and starts with", best_number)
