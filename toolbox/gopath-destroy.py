#! /usr/bin/env python3

import argparse
import os, os.path
import shutil
import sys

if __name__ == '__main__':
    prgDir = os.path.dirname(os.path.realpath(sys.argv[0]))
    pyPathDir = os.path.join(prgDir, 'PyPath')
    sys.path.append(pyPathDir)

from toolbox import golang


def parseArgs():
    parser = argparse.ArgumentParser(
            description='Delete $GOPATH and create it as an empty directory')
    return parser.parse_args()


if __name__ == '__main__':
    parseArgs()
    gopath = golang.getGOPATH()
    if os.path.exists(gopath):
        shutil.rmtree(gopath)
    os.mkdir(gopath)
