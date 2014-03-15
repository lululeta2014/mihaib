# Find the difference between the sum of the squares
# of the first one hundred natural numbers and the square of the sum
# (to be more precise: 1 to 100)

if __name__ == '__main__':
    s1 = s2 = 0
    for n in range(1, 101):
        s1 += n * n
    s2 = n * (n + 1) // 2
    s2 *= s2
    print(abs(s1 - s2))
