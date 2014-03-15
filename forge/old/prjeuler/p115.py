#! /usr/bin/env python3

def f(block_size=50):
    w = []
    for i in range(block_size):
        w.append(1)

    i = block_size
    while (True):
        # red block of length i
        x = 1
        # black block
        x = x + w[i-1]

        # red block of length j followed by black block
        for j in range(block_size, i):
            x = x + w[i - j - 1]

        w.append(x)

        if x > 1000000:
            return i

        i = i + 1

if __name__ == '__main__':
    print(f())
