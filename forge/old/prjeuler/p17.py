# If all the numbers from 1 to 1000 (one thousand) inclusive
# were written out in words, how many letters would be used?
# Do not count spaces or hyphens.
# For example, 342 (three hundred and forty-two) contains 23 letters
# and 115 (one hundred and fifteen) contains 20 letters

def count_letters(n):
    if n < 1 or n >= 1000:
        raise ValueError('Number', n, 'not between 1 and 999')

    simple=["", "one", "two", "three", "four", "five", "six",
            "seven", 'eight', 'nine', 'ten', 'eleven', 'twelve',
            'thirteen', 'fourteen', 'fifteen', 'sixteen',
            'seventeen', 'eighteen', 'nineteen']
    tens = [0, 0, 'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy',
            'eighty', 'ninety']

    t = n % 100
    h = n // 100

    length = 0
    if h > 0:
        length += len(simple[h]) + len("hundred")
        if t > 0:
            length += len("and")
    if t > 0:
        if t < 20:
            length += len(simple[t])
        else:
            length += len(tens[t // 10]) + len(simple[t % 10])
    return length

if __name__ == '__main__':
    l = 0
    for i in range(1, 1000):
        l += count_letters(i)
    l += len("one") + len('thousand')
    print(l)
