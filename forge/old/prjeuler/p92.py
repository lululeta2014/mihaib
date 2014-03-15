square_digits = [i * i for i in range(10)]
dest = [0 for i in range(81 * 7 + 1)]
dest[0] = -1
dest[1] = 1
dest[89] = 89

def sum_dig_sq(n):
    s = 0
    while n > 0:
        s += square_digits[n % 10]
        n //= 10
    return s

def stuck(n):
    if n >= len(dest):
        n = sum_dig_sq(n)

    L = []
    while dest[n] == 0:
        L.append(n)
        n = sum_dig_sq(n)
    for x in L:
        dest[x] = dest[n]

    return dest[n]

if __name__ == '__main__':
    count = 0
    for x in range(1, 10 ** 7):
        if stuck(x) == 89:
            count += 1
    print(count)
