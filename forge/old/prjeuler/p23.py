# Find the sum of all the positive integers
# which cannot be written as the sum of two abundant numbers.

from prjeuler import sum_of_divisors

def is_abundant(n):
    return sum_of_divisors(n) > n

if __name__ == '__main__':
    possible_sums = set()
    abundant_nums = []

    upper_limit = 28123

    for n in range(1, upper_limit + 1):
        if is_abundant(n):
            abundant_nums.append(n)
            for x in abundant_nums:
                s = n + x
                if s > upper_limit:
                    break
                possible_sums.add(s)

    s = 0
    for n in range(1, upper_limit + 1):
        if n not in possible_sums:
            s += n
    print(s)
