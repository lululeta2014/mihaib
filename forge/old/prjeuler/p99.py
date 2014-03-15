from math import log

if __name__ == '__main__':
    with open('p99-base-exp.txt') as f:
        lines = f.readlines()

    pairs = [tuple (int(n) for n in line.strip().split(',')) for line in lines]
    log_values = [e * log(b) for (b, e) in pairs]
    index = max_val = -1
    for i, val in enumerate(log_values):
        if val > max_val:
            max_val = val
            index = i
    print(index + 1)
