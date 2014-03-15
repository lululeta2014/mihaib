if __name__ == '__main__':
    upper_limit = 1
    pow9 = 9 ** upper_limit
    pow10 = 10 ** (upper_limit - 1)
    while pow9 >= pow10:
        upper_limit += 1
        pow9 *= 9
        pow10 *= 10

    low, high = 1, 10
    count = 0
    vals = [1 for i in range(1, 10)]
    for n in range(1, upper_limit):
        vals = [(i + 1) * v for i, v in enumerate(vals)]
        for v in vals:
            if v >= low and v < high:
                count += 1
        low, high = low*10, high*10
    print(count)
