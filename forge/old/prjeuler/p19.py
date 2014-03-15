# How many Sundays fell on the first of the month
# during the twentieth century (1 Jan 1901 to 31 Dec 2000)?
# 1 Jan 1900 was a Monday.

def days_in_month(month, year):
    '''Returns the days in the specified month/year'''
    if month < 1 or month > 12 or year < 1:
        raise ValueError('month {0} year {1} not valid\
            month must be between 1 and 12 and year >= 1'.format(month, year))
    if month == 2:
        if year % 4 != 0:
            return 28
        elif year % 100 != 0:
            return 29
        elif year % 400 != 0:
            return 28
        else:
            return 29
    elif month in [1, 3, 5, 7, 8, 10, 12]:
        return 31
    else:
        return 30

if __name__ == '__main__':
    crt_day = 0
    count = 0

    # find 1 Jan 1901
    for month in range(1, 13):
        crt_day += days_in_month(month, 1900)
    crt_day %= 7
    if crt_day == 6:
        count = 1

    # count sundays
    for year in range(1901, 2001):
        for month in range(1, 13):
            crt_day += days_in_month(month, year)
            crt_day %= 7
            if crt_day == 6:
                count += 1
    # last day is 1 Jan 2001
    if crt_day == 6:
        count -= 1
    print(count)
