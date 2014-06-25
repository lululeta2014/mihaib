#! /usr/bin/env python3

# For each unicode character given, print it, its codepoint U+xxxx, its name
# and its UTF-8 bytes.
# Characters can be given either as arguments or, if no arguments are given,
# read from standard input.
# Therefore, to avoid ambiguity, the program doesn't interpret any command line
# flags, not even -h or --help.

import sys
import unicodedata


def describe(ch):
    return '{0!r:<4} U+{1:04X}  {2}  [{3}]'.format(ch, ord(ch),
            unicodedata.name(ch, '¡no such name!'),
            ' '.join('{:02x}'.format(b) for b in ch.encode('utf-8')))

if __name__ == '__main__':
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            for ch in arg:
                print(describe(ch))
    else:
        while True:
            ch = sys.stdin.read(1)
            if not ch:
                break
            print(describe(ch))
