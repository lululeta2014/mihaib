# Find the largest palindrome made from the product of two 3-digit numbers.

from prjeuler import get_base_10_digits

def isPalindrome(l):
    '''Checks if a list is a palindrome, returning True or False.'''
    lr = l[:]
    lr.reverse()
    return l == lr

if __name__ == '__main__':
    maxProd = 0
    for n1 in range(100, 1000):
        for n2 in range(n1, 1000):
            prod = n1 * n2
            if prod > maxProd and isPalindrome(get_base_10_digits(prod)):
                maxProd = prod
    print(maxProd)
