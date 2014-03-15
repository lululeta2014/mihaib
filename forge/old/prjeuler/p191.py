# a tuple is the history for a child, ending in the current day
# with the elements (L, A) == (late, absence_suffix)
# L = 0 if never late, 1 if child came late once
# A = 0, 1, or 2: how many consecutive days absent, including today
# a day maps these tuples to numbers, so we know
# how many different strings are available with the same values for L and A

if __name__ == '__main__':
    t = (0, 0)
    today = {t:1}

    n = 30
    for i in range(n):
        yesterday = today
        today = {}

        for y in yesterday:
            val = yesterday[y]
            (yl, ya) = y
            if yl == 0:
                t = (1, 0)
                if t in today:
                    today[t] = today[t] + val
                else:
                    today[t] = val
            if ya < 2:
                t = (yl, ya + 1)
                if t in today:
                    today[t] = today[t] + val
                else:
                    today[t] = val
            t = (yl, 0)
            if t in today:
                today[t] = today[t] + val
            else:
                today[t] = val

    print(sum(today.values()))
