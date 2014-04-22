#! /usr/bin/env python3

import argparse
import os, os.path
import re
import sys

if __name__ == '__main__':
    prgDir = os.path.dirname(os.path.realpath(sys.argv[0]))
    pyPathDir = os.path.join(os.path.dirname(prgDir), 'PyPath')
    sys.path.append(pyPathDir)

from toolbox import comp9


def parseArgs():
    p = argparse.ArgumentParser(description='''Rename files
        starting with a date-stamp prefix
        such that sorting them lexicographically ascending
        arranges them in reverse chronological order''')
    p.add_argument('rootdir', help='Root of directory tree to process')
    return p.parse_args()


def processTree(root):
    for name in os.listdir(root):
        path = os.path.join(root, name)
        path = moveIfNeeded(path)
        if os.path.isdir(path):
            processTree(path)


def moveIfNeeded(path):
    parent, name = os.path.split(path)
    if matchesPattern(name):
        newPath = os.path.join(parent, getNewName(name))
        if os.path.lexists(newPath):
            raise ValueError('While renaming ' + path + ': '
                    + newPath + ' already exists!')
        os.rename(path, newPath)
        return newPath
    return path


def matchesPattern(name):
    return True if pattern.match(name) else False

pattern = re.compile('^[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}([^0-9]|$)')


def getNewName(name):
    m = pattern.match(name)
    if not m:
        raise ValueError(name + ' doesn\'t match the required pattern')
    start, end = m.span()
    if start != 0:
        raise ValueError('Internal Error: pattern doesn\'t match '
                + 'at start of filename')

    result = ''
    for x in name[:end]:
        # convert only digits
        try:
            result += comp9.num2chr(comp9.comp9(int(x)), 'α')
        except ValueError:
            pass
    return result + '-' + name


if __name__ == '__main__':
    args = parseArgs()
    processTree(args.rootdir)
