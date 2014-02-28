#! /usr/bin/env python3

import argparse
import os, os.path
import sys

if __name__ == '__main__':
    prgDir = os.path.dirname(os.path.realpath(sys.argv[0]))
    pyPathDir = os.path.join(prgDir, 'PyPath')
    sys.path.append(pyPathDir)

from toolbox import golang
from toolbox.util import printExit


def parseArgs():
    parser = argparse.ArgumentParser(description='symlink from $GOPATH')
    parser.add_argument('package', help='full/package/name')
    parser.add_argument('target', help='target of symlink')
    parser.add_argument('--replace', action='store_true',
            help='Remove symlink if present, before creating it')
    args = parser.parse_args()

    if os.path.isabs(args.package):
        printExit('package', args.package, 'is an absolute (filesystem) path')
    if not args.package or not args.target:
        printExit('package or target are empty')
    return args


def checkNoExistingSymlinks(package, gopath_src):
    packageParts = package.split('/')
    pDir = gopath_src
    for part in packageParts[:-1]:
        pDir = os.path.join(pDir, part)
        if not os.path.lexists(pDir):
            break
        if os.path.islink(pDir):
            printExit('Intermediate symlink found', pDir)


def getGopathSrc():
    gopath = golang.getGOPATH()
    if not os.path.exists(gopath):
        printExit('GOPATH', gopath, 'does not exist')
    return os.path.join(gopath, 'src')


if __name__ == '__main__':
    args = parseArgs()
    gopath_src = getGopathSrc()
    checkNoExistingSymlinks(args.package, gopath_src)

    os.makedirs(os.path.join(gopath_src, os.path.dirname(args.package)),
            exist_ok=True)

    if not os.path.isabs(args.target):
        absTarget = os.path.abspath(args.target)
        print('Converting', args.target, '→', absTarget)
        args.target = absTarget
    linkPath = os.path.join(gopath_src, args.package)
    if args.replace and os.path.islink(linkPath):
        os.remove(linkPath)
    os.symlink(args.target, linkPath)
