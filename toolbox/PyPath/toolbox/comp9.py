"""
9's complement conversion (x → 9-x) and
number-to-alpha conversion (x → 'A' + x-0).
"""

def comp9(x):
    """
    9's complement (9-x) if x is an int between 0 and 9, else ValueError.
    """
    if type(x) == int and 0 <= x and x <= 9:
        return 9-x
    raise ValueError(str(x) + ' must be int, ≥0 and ≤9')


def num2alpha(x, lowerCase=False):
    """
    0→A, 1→B … 9→J, ValueError for any other x.

    lowerCase changes the returned value from A…J to a…j.
    """
    if type(x) == int and 0 <= x and x <= 9:
        return chr(ord('a' if lowerCase else 'A') + x)
    raise ValueError(str(x) + ' must be int, ≥0 and ≤9')
