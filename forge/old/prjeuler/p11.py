# What is the greatest product of four adjacent numbers in any direction
# (horiz, vert or diag) in the 20x20 grid below?

data_p11_str = "\
08 02 22 97 38 15 00 40 00 75 04 05 07 78 52 12 50 77 91 08\
 49 49 99 40 17 81 18 57 60 87 17 40 98 43 69 48 04 56 62 00\
 81 49 31 73 55 79 14 29 93 71 40 67 53 88 30 03 49 13 36 65\
 52 70 95 23 04 60 11 42 69 24 68 56 01 32 56 71 37 02 36 91\
 22 31 16 71 51 67 63 89 41 92 36 54 22 40 40 28 66 33 13 80\
 24 47 32 60 99 03 45 02 44 75 33 53 78 36 84 20 35 17 12 50\
 32 98 81 28 64 23 67 10 26 38 40 67 59 54 70 66 18 38 64 70\
 67 26 20 68 02 62 12 20 95 63 94 39 63 08 40 91 66 49 94 21\
 24 55 58 05 66 73 99 26 97 17 78 78 96 83 14 88 34 89 63 72\
 21 36 23 09 75 00 76 44 20 45 35 14 00 61 33 97 34 31 33 95\
 78 17 53 28 22 75 31 67 15 94 03 80 04 62 16 14 09 53 56 92\
 16 39 05 42 96 35 31 47 55 58 88 24 00 17 54 24 36 29 85 57\
 86 56 00 48 35 71 89 07 05 44 44 37 44 60 21 58 51 54 17 58\
 19 80 81 68 05 94 47 69 28 73 92 13 86 52 17 77 04 89 55 40\
 04 52 08 83 97 35 99 16 07 97 57 32 16 26 26 79 33 27 98 66\
 88 36 68 87 57 62 20 72 03 46 33 67 46 55 12 32 63 93 53 69\
 04 42 16 73 38 25 39 11 24 94 72 18 08 46 29 32 40 62 76 36\
 20 69 36 41 72 30 23 88 34 62 99 69 82 67 59 85 74 04 36 16\
 20 73 35 29 78 31 90 01 74 31 49 71 48 86 81 16 23 57 05 54\
 01 70 54 71 83 51 54 69 16 92 33 48 61 43 52 01 89 19 67 48";

zeroVal = '0'.encode('utf-8')[0]

data_p11_num = [int(s) for s in data_p11_str.split(' ')]

def get_row_size(elem_count, rows):
    if elem_count % rows:
        raise ValueError('list size {0} not multiple of row count {1}'
                .format(elem_count, rows))
    return elem_count // rows

def get_lines(num_list, rows):
    row_size = get_row_size(len(num_list), rows)
    return [num_list[i*row_size:(i+1)*row_size] for i in range(rows)]

def get_cols(num_list, rows):
    cols = get_row_size(len(num_list), rows)
    return [[num_list[j] for j in range(i, len(num_list), cols)]
            for i in range(cols)]

def get_diags1(num_list, rows):
    cols = get_row_size(len(num_list), rows)
    # diagonals starting on first row
    diag_r1 = [[num_list[r * cols + (c + r)]
                for r in range(min(rows, cols - c))]
            for c in range(cols)]
    # diagonals starting on first column, without first
    diag_c1 = [[num_list[(r + c) * cols + c]
                for c in range(min(rows - r, cols))]
            for r in range(1, rows)]
    return diag_r1 + diag_c1

def get_diags2(num_list, rows):
    cols = get_row_size(len(num_list), rows)
    #flip lines horizontally
    num_list = [num_list[r * cols + (cols - c - 1)]
            for r in range(rows) for c in range(cols)]
    # diagonals starting on first row
    diag_r1 = [[num_list[r * cols + (c + r)]
                for r in range(min(rows, cols - c))]
            for c in range(cols)]
    # diagonals starting on first column, without first
    diag_c1 = [[num_list[(r + c) * cols + c]
                for c in range(min(rows - r, cols))]
            for r in range(1, rows)]
    return diag_r1 + diag_c1

def get_max_4(num_list):
    if len(num_list) < 4:
        return 0
    products = [num_list[i] * num_list[i + 1]
            * num_list[i + 2] * num_list[i + 3]
            for i in range(len(num_list) - 3)]
    if (max(products) == 1788696):
        print(max(products))
    return max(products)

if __name__ == '__main__':
    lines = get_lines(data_p11_num, 20)
    cols = get_cols(data_p11_num, 20)
    diags1 = get_diags1(data_p11_num, 20)
    diags2 = get_diags2(data_p11_num, 20)

    max_line_prod = max([get_max_4(l) for l in lines])
    max_col_prod = max([get_max_4(c) for c in cols])
    max_diag1_prod = max([get_max_4(d) for d in diags1])
    max_diag2_prod = max([get_max_4(d) for d in diags2])
    print(max(max_line_prod, max_col_prod, max_diag1_prod, max_diag2_prod))
