# Considering natural numbers of the form, a^(b), where a, b < 100,
# what is the maximum sum of their digits?

from prjeuler import get_base_10_digits

if __name__ == '__main__':
    s = 0
    for a in range(1, 100):
        for b in range(1, 100):
            crt_sum = sum(get_base_10_digits(a ** b))
            if crt_sum > s:
                s = crt_sum
    print(s)
