# Find the sum of all the even-valued terms in the Fibonacci sequence
# which do not exceed four million.

# Observation: even, odd, odd, even, odd, odd etc.

if __name__ == '__main__':
    sum = 0
    a, b = 1, 2
    while b < 4000000:
        sum += b
        for i in range(3):
            a, b = b, a + b
    print(sum)
