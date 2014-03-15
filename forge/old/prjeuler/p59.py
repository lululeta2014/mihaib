if __name__ == '__main__':
    A_byte = 'A'.encode('ascii')[0]
    Z_byte = 'Z'.encode('ascii')[0]
    a_byte = 'a'.encode('ascii')[0]
    z_byte = 'z'.encode('ascii')[0]

    lc_letters = {x for x in range(a_byte, z_byte + 1)}
    uc_letters = {x for x in range(A_byte, Z_byte + 1)}
    letters = set()
    letters.update(lc_letters)
    letters.update(uc_letters)
    symbols_str = ' \t.,:;\'"!?-\r\n0123456789(){}[]<>/\\+-=&*#$%'
    symbols = {x for x in symbols_str.encode('ascii')}

    with open('p59-cipher1.txt', 'r') as f:
        file_line = f.readline().strip()
    src = [int(x) for x in file_line.split(',')]

    key = list(range(3))
    for key[0] in lc_letters:
        for key[1] in lc_letters:
            for key[2] in lc_letters:
                dest = []
                key_valid = True
                for i, v in enumerate(src):
                    x = v ^ key[i % 3]
                    if x in letters or x in symbols:
                        dest.append(x)
                    else:
                    #    print("bad", x, "'", bytes([x]).decode('ascii'), "'")
                        key_valid = False
                        break
                if key_valid:
                    print(bytes(dest).decode('ascii'))
                    print("key", bytes(key).decode('ascii'))
                    print("ascii sum:", sum(dest))
