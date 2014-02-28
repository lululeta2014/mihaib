#! /usr/bin/env python3

import argparse
import collections
import json
import sys


def parseArgs():
    parser = argparse.ArgumentParser(description='Pretty-Print JSON')
    parser.add_argument('file', default=None, nargs='?',
            help='JSON file to read. Reads from STDIN if omitted.')
    parser.add_argument('-i', '--indent', default=2, type=int, metavar='N',
            help='How many spaces to indent with. Default %(default)s.')
    parser.add_argument('--ascii', default=False, action='store_true',
            help='Escape all non-ASCII characters. Default %(default)s.')
    return parser.parse_args()


if __name__ == '__main__':
    args = parseArgs()
    with open(args.file, encoding='utf-8') if args.file else sys.stdin as f:
        data = json.load(f, object_pairs_hook=collections.OrderedDict)
    print(json.dumps(data, indent=args.indent, ensure_ascii=args.ascii,
        separators=(',', ': ')))
