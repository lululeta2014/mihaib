def get_digits(n):
    digits = []
    while n > 0:
        digits.append(n % 10)
        n //= 10
    return digits

if __name__ == '__main__':
    a, b = 1, 1
    i = 2
    target_list = [i for i in range(1, 10)]
    while True:
        a, b = b, a + b
        i += 1
        digits = get_digits(b)
        d1 = sorted(digits[:9])
        d2 = sorted(digits[-9:])
        if d1 == target_list and d2 == target_list:
            break
    print(i)
    print(b)
