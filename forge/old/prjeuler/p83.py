if __name__ == '__main__':
    with open('p83-matrix.txt') as f:
        lines = f.readlines()
    matrix = [[int(s) for s in line.strip().split(',')] for line in lines]
    rows = len(matrix)
    cols = len(matrix[0])
    cost = [[0 for e in range(cols)] for r in range(rows)]

    # populate cost
    cost[0][0] = matrix[0][0]
    # first column
    for r in range(1, rows):
        cost[r][0] = cost[r - 1][0] + matrix[r][0]
    # all rows
    for r in range(0, rows):
        for c in range(1, cols):
            cost[r][c] = cost[r][c - 1] + matrix[r][c]

    changed = True
    while changed:
        changed = False
        # down
        for r in range(rows - 1):
            for c in range(cols):
                new_cost = cost[r][c] + matrix[r + 1][c]
                if new_cost < cost[r + 1][c]:
                    changed = True
                    cost[r + 1][c] = new_cost
        # up
        for r in range(rows - 1):
            for c in range(cols):
                new_cost = cost[-1 - r][c] + matrix[-1 - r - 1][c]
                if new_cost < cost [-1 - r - 1][c]:
                    changed = True
                    cost[-1 - r - 1][c] = new_cost
        # right
        for r in range(rows):
            for c in range(cols - 1):
                new_cost = cost[r][c] + matrix[r][c + 1]
                if new_cost < cost[r][c + 1]:
                    changed = True
                    cost[r][c + 1] = new_cost
        # left
        for r in range(rows):
            for c in range(cols - 1):
                new_cost = cost[r][-1 - c] + matrix[r][-1 - c - 1]
                if new_cost < cost[r][-1 - c - 1]:
                    changed = True
                    cost[r][-1 - c - 1] = new_cost

    print(cost[-1][-1])
