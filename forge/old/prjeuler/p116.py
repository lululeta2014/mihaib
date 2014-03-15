#! /usr/bin/env python3

def ways(tile_size, row_size = 50):
    l = []
    for i in range(tile_size):
        l.append(1)

    for i in range(tile_size, row_size + 1):
        l.append(l[i-1] + l[i-tile_size])

    return l[row_size] - 1

print(ways(2) + ways(3) + ways(4))
