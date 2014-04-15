#! /usr/bin/env python3

import argparse
import os, os.path
import random
import sys


def parseArgs():
    p = argparse.ArgumentParser(description='''Choose a file, randomly,
        from the specified directory and write its name followed by a newline
        to stdout. The chosen file is either a regular file or a symlink
        to a regular file and is located in the directory itself not in a
        subdirectory.''')
    p.add_argument('dir', help='The directory to pick a file from')
    return p.parse_args()


if __name__ == '__main__':
    args = parseArgs()
    files = [f for f in os.listdir(args.dir)
            if os.path.isfile(os.path.join(args.dir, f))]
    if not len(files):
        print('No files found in', args.dir, file=sys.stderr)
        sys.exit(1)
    print(random.choice(files))
