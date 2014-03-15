values = {'I':1, 'V':5, 'X':10, 'L':50, 'C':100, 'D':500, 'M':1000}

def rm2nr(rm):
    rm = rm.upper()
    nr = 0
    last = 1000
    for c in rm:
        v = values[c]
        if v == None:
            raise ValueError("Invalid character `{0}' in roman nr".format(c))
        nr += v
        if last < v:
            nr -= 2 * last
        last = v
    return nr

def nr2rm(nr):
    if nr <= 0:
        raise ValueError("Can only convert numbers >= 0")
    result = ''
    dig = nr % 10
    nr //= 10
    if dig != 0:
        if dig == 4:
            result = 'IV'
        elif dig == 9:
            result = 'IX'
        else:
            if dig >= 5:
                result = 'V'
                dig -= 5
            for i in range(dig):
                result += 'I'

    if nr == 0:
        return result
    dig = nr % 10
    nr //= 10
    if dig != 0:
        if dig == 4:
            result = 'XL' + result
        elif dig == 9:
            result = 'XC' + result
        else:
            tmp = ''
            if dig >= 5:
                tmp = 'L'
                dig -= 5
            for i in range(dig):
                tmp += 'X'
            result = tmp + result

    if nr == 0:
        return result
    dig = nr % 10
    nr //= 10
    if dig != 0:
        if dig == 4:
            result = 'CD' + result
        elif dig == 9:
            result = 'CM' + result
        else:
            tmp = ''
            if dig >= 5:
                tmp = 'D'
                dig -= 5
            for i in range(dig):
                tmp += 'C'
            result = tmp + result

    for i in range(nr):
        result = 'M' + result

    return result

if __name__ == '__main__':
    with open('p89-roman.txt') as f:
        lines = f.readlines()

    src = [line.strip() for line in lines]
    dec = [rm2nr(rm) for rm in src]
    dest = [nr2rm(nr) for nr in dec]

    saved = [len(s) - len(d) for s, d in zip(src, dest)]
    print(sum(saved))
