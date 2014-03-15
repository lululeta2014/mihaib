#! /usr/bin/env python3

def ways(row_len = 50):
    w = [1]
    for i in range(1, row_len+1):
        x = w[i-1]
        for k in (2, 3, 4):
            if i >= k:
                x = x + w[i-k]
        w.append(x)
    return w[row_len]

if __name__ == '__main__':
    print(ways())
