if __name__ == '__main__':
    with open('p82-matrix.txt', 'r') as f:
        lines = f.readlines()
    matrix = [[int(x) for x in row.strip().split(',')] for row in lines]

    cost = [[0 for elem in row] for row in matrix]
    for c in range(len(matrix[0])):
        # move from from column c-1 right to column c
        for r in range(len(matrix)):
            if c == 0:
                cost[r][c] = matrix[r][c]
            else:
                cost[r][c] = matrix[r][c] + cost[r][c - 1]
        # check move down, from top to bottom
        for r in range(len(matrix) - 1):
            cost[r + 1][c] = min(cost[r + 1][c], cost[r][c] + matrix[r + 1][c])
        # check move up, from bottom to top
        for r in range(len(matrix) - 1, 0, -1):
            cost[r - 1][c] = min(cost[r - 1][c], cost[r][c] + matrix[r - 1][c])

    print(min([row[-1] for row in cost]))
