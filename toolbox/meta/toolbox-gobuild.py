#! /usr/bin/env python3

import argparse
import os, os.path
import subprocess
import sys


def parseArgs():
    p = argparse.ArgumentParser(
            description='''Link this Toolbox in GOPATH, download dependencies,
            run tests and build Toolbox Go Package''')
    p.add_argument('cmdLineDir', metavar='directory',
            help='''Path (absolute or relative to the toolbox root)
            to directory of Go package to build.''')
    p.add_argument('output_file', nargs='?',
            help='optional output file name (passed as -o to ‘go build’)')

    args = p.parse_args()

    toolboxRoot = getToolboxRoot()
    # this should strip trailing slashes
    args.dirAbsPath = os.path.normpath(
            os.path.join(toolboxRoot, args.cmdLineDir))
    args.dirRelPath = os.path.relpath(args.dirAbsPath, toolboxRoot)

    return args


def getProgramDir():
    programFile = os.path.realpath(sys.argv[0])
    return os.path.dirname(programFile)


def getToolboxRoot():
    return os.path.dirname(getProgramDir())


def toolboxGoLink():
    toolboxRoot = getProgramDir()
    subprocess.check_call([os.path.join(toolboxRoot, 'toolbox-golink.sh')])


def goBuild(dirRelPath, outFile=None):
    """Download dependencies and build package, with optional command name."""
    pkg = os.path.join('github.com/MihaiB/toolbox', dirRelPath)
    subprocess.check_call(['go', 'get', '-d', pkg])
    subprocess.check_call(['go', 'test', pkg])
    buildCmd = ['go', 'build']
    if outFile:
        buildCmd.append('-o')
        buildCmd.append(outFile)
    buildCmd.append(pkg)
    pkgAbsDir = os.path.join(getToolboxRoot(), dirRelPath)
    subprocess.check_call(buildCmd, cwd=pkgAbsDir)


if __name__ == '__main__':
    args = parseArgs()
    toolboxGoLink()
    goBuild(args.dirRelPath, args.output_file)
