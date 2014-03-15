if __name__ == '__main__':
    with open('p81-matrix.txt', 'r') as f:
        lines = f.readlines()
    matrix = [[int(i) for i in line.strip().split(',')] for line in lines]
    cost = [[0 for e in line] for line in matrix]
    for i,line in enumerate(matrix):
        for j, v in enumerate(line):
            if i == 0:
                if j == 0:
                    cost[i][j] = v
                else:
                    cost[i][j] = v + cost[i][j - 1]
            else:
                if j == 0:
                    cost[i][j] = v + cost[i - 1][j]
                else:
                    cost[i][j] = v + min(cost[i][j - 1], cost[i - 1][j])
    print(cost[-1][-1])

#    cost = [
#            [(v + min(cost[i][j - 1], cost[i - 1][j])
#                    if j > 0 else
#                    v + cost[i - 1][j])
#            if i > 0 else
#                    (v + cost[i][j - 1] if j > 0 else v)
#            for j, v in enumerate(r)]
#            for i, r in enumerate(matrix)]
#    print(cost[-1][-1])
