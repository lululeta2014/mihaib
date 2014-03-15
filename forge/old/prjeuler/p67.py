# Find the maximum total from top to bottom of the triangle
# copy/paste of problem 18

def max_sum(L):
    if len(L) == 0:
        return 0
    n = 1
    sums = L[:n]
    L = L[n:]
    
    while L:
        n += 1
        if n > len(L):
            raise ValueError("The list is not a triangle")
        row = L[:n]
        L = L[n:]
        sums = [row[0] + sums[0]]\
            + [max(v + sums[i], v + sums[i + 1])
               for i, v in enumerate(row[1:-1])]\
            + [row[-1] + sums[-1]]

    return max(sums)

if __name__ == '__main__':
    data_str = ''
    with open('p67-triangle.txt') as f:
        lines = f.readlines()
        for line in lines:
            data_str = data_str + (line.strip() + ' ')
    data_str = data_str.strip()
    L = [int(nr) for nr in data_str.split(" ")]
    print(max_sum(L))
