def inc_digits(L):
    if L[-1] <= L[0]:
        L[-1] = L[0] + 1
        return
    t = 2
    for i in range(-1, -len(L) - 1, -1):
        if t == 0:
            return
        s = L[i] + t
        L[i] = s % 10
        t = s // 10

def reversible(L):
    t = 0
    for i in range(len(L)):
        s = L[i] + L[-1 - i] + t
        if s % 2 == 0:
            return False
        t = s // 10
    return True

if __name__ == '__main__':
    count = 0
    after_max_len = 10
    for crtLen in range(2, after_max_len):
        print(crtLen)
        digits = [1] + [0 for x in range(crtLen - 1)]
        # 1(0)+ is not reversible, so it's safe to increment without checking
        inc_digits(digits)
        while digits[0] != 9:
            if digits[0] < digits[-1] and reversible(digits):
                    count += 2
            inc_digits(digits)
    print(count)
