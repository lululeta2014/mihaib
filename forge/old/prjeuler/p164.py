# the dictionaries hold (some) keys between 0 and 99
# dict[ab] = how many valid numbers end in ab where a and b are digits

# we start with a dictionary for the first digit of the number:
# crtDict[1] = crtDict[2] = .. = crtDict[9] = 1
# and compute 19 dictionaries for the next 19 digits of the number
# we don't need to remember all 20 dictionaries, just one

if __name__ == '__main__':
    crtDict = {}
    for i in range(1, 10):
        crtDict[i] = 1

    for i in range(19):
        prevDict = crtDict
        crtDict = {}

        for a in range(10):
            for b in range(10):

                if (a * 10 + b in prevDict):
                    prevVal = prevDict[a * 10 + b]
                    for c in range(10 - a - b):
                        if b*10 + c in crtDict:
                            crtDict[b*10 + c] = crtDict[b*10 + c] + prevVal
                        else:
                            crtDict[b*10 + c] = prevVal

    print(sum(crtDict.values()))
