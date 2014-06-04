#! /usr/bin/env python3

import argparse
import random
import sys

def parseArgs():
    p = argparse.ArgumentParser(description='''Exit with code 0 (success)
        with K out of N probability''')
    p.add_argument('K', type=int, help='probability is K out of N')
    p.add_argument('N', type=int, help='probability is K out of N')
    args = p.parse_args()
    if args.K < 0:
        print('K must be ≥ 0', file=sys.stderr)
        sys.exit(2)
    if args.N < 1:
        print('N must be ≥ 1', file=sys.stderr)
        sys.exit(2)
    if args.K > args.N:
        print('K must be ≤ N', file=sys.stderr)
        sys.exit(2)
    return args


if __name__ == '__main__':
    args = parseArgs()
    sys.exit(0 if random.randrange(args.N) < args.K else 1)
