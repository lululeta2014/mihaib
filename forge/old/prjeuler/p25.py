# What is the first term in the Fibonacci sequence to contain 1000 digits?

if __name__ == '__main__':
    target = 10 ** 999
    a, b, b_index = 1, 1, 2
    while b < target:
        a, b = b, a + b
        b_index += 1
    print(b_index)
