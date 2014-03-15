def isPal(L):
    for i in range(len(L) // 2):
        if (L[i] != L[-1 - i]):
            return False
    return True

def getDigits(n, b):
    result = []
    while n > 0:
        result.append(n % b)
        n //= b
    return result;

if __name__ == '__main__':
    count = 0
    s = 0

    for n in range(1000 * 1000):
        if (isPal(getDigits(n, 10)) and isPal(getDigits(n, 2))):
            count += 1
            s += n

    print(count, 'numbers')
    print(s, 'sum')
