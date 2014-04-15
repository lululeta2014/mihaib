#! /usr/bin/env python3

import argparse
import os, os.path
import subprocess
import sys

def parseArgs():
    p = argparse.ArgumentParser(description='Set a random wallpaper from dir')
    p.add_argument('dir', help='Directory to pick the wallpaper from')
    p.add_argument('moreArgs', nargs='*', help='''Remaining arguments,
        passed to set-wallpaper.py unchanged''')
    return p.parse_args()

if __name__ == '__main__':
    args = parseArgs()
    prgDir = os.path.dirname(os.path.realpath(sys.argv[0]))
    imgFile = (subprocess.check_output(['pick-random-file', args.dir])
            .decode('utf-8').strip())
    imgFile = os.path.join(args.dir, imgFile)

    execArgs = [os.path.join(prgDir, 'set-wallpaper.py'), imgFile]
    execArgs.extend(args.moreArgs)
    subprocess.check_call(execArgs)
