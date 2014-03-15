#! /usr/bin/env python3

def ways(row_len = 50):
    w = []
    for i in range(3):
        w.append(1)

    for i in range(3, row_len + 1):
        # red block of length i
        x = 1
        # black block
        x = x + w[i-1]

        # red block of length j followed by black block
        for j in range(3, i):
            x = x + w[i - j - 1]

        w.append(x)

    return w[row_len]

if __name__ == '__main__':
    print(ways())
