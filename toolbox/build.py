#! /usr/bin/env python3

import argparse
import json
import os, os.path
import shutil
import subprocess
import sys
import unicodedata


BOOTSTRAP_CHAR = '⌘';

def isBootstrapItem(text):
    return len(text) and text[0] == BOOTSTRAP_CHAR

def unBootstrap(text):
    '''Remove the bootstrap prefix char, if present.'''
    return text[1:] if isBootstrapItem(text) else text


def parseArgs():
    p = argparse.ArgumentParser(description='''Delete bin/, create symlinks in
            bin/ and build all programs according to build.json.
            Symlink before building because some tools depend on others (e.g.
            on download-kit).
            Symlinks are just a filename created in bin/. The paths
            for everything else are relative to this root directory.''')
    p.add_argument('--bootstrap', action='store_true',
            help='''Run in ‘bootstrap mode’. Symlink and build only items
            starting with “''' + BOOTSTRAP_CHAR + '” '
            + 'U+{:04X}'.format(ord(BOOTSTRAP_CHAR))
            + ' (' + unicodedata.name(BOOTSTRAP_CHAR, '¡Not Found!') + ').'
            + ''' This character is only used to mark bootstrap items, and is
            removed before processing each item.
            Bootstrap mode should not need an Internet connection.''')
    return p.parse_args()


def getScriptDir():
    '''May return the empty string, e.g. for ‘python3 build.py’'''
    return os.path.dirname(sys.argv[0])


def getBinDir():
    return os.path.join(getScriptDir(), 'bin')


def putBinInPath():
    """Prepend bin/ directory to PATH."""
    os.environ['PATH'] = getBinDir() + os.pathsep + os.environ['PATH']


def getConfig() :
    cfgPath = os.path.join(getScriptDir(), 'build.json')
    with open(cfgPath, 'r', encoding='utf-8') as config:
        return json.load(config)


def makeLinks(config, bootstrap):
    rootDir = getScriptDir()
    binDir = getBinDir()
    if os.path.exists(binDir):
        shutil.rmtree(binDir)
    os.mkdir(binDir)

    for key, val in config['link'].items():
        if not bootstrap or isBootstrapItem(key):
            key = unBootstrap(key)
            link = os.path.join(binDir, key)
            target = os.path.join(rootDir, val)
            route = os.path.relpath(target, binDir)
            os.symlink(route, link)


def prettyCheckCall(prettyName, *args, **kwargs):
    """Calls subprocess.check_call(*args, **kwargs) and prints prettyName."""
    success = False
    try:
        subprocess.check_call(*args, **kwargs)
        success = True
    finally:
        print('✓' if success else '✗', prettyName)


def build(config, bootstrap):
    rootDir = getScriptDir()
    for builder in config['build']:
        if not bootstrap or isBootstrapItem(builder):
            builder = unBootstrap(builder)
            prettyCheckCall(builder, [os.path.join(rootDir, builder)])


if __name__ == '__main__':
    args = parseArgs()
    putBinInPath()
    config = getConfig()
    makeLinks(config, args.bootstrap)
    build(config, args.bootstrap)
