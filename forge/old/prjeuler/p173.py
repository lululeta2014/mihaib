if __name__ == '__main__':
    A = []

    size = 3
    B = [4 * (size-1)]
    solutions = 1

    size = 4
    L = [4 * (size-1)]
    solutions += 1

    limit = 1000000
    size += 1
    while 4 * (size-1) <= limit:
        A = B
        B = L
        L = [4 * (size-1)]
        solutions += 1

        for a in A:
            newVal = L[0] + a
            if newVal <= limit:
                L.append(newVal)
                solutions += 1
            else:
                break

        size += 1

    print(solutions)
