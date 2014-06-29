#! /usr/bin/env python3

import os, os.path
import subprocess
import sys


EXT_DIR_NAME = 'extension-root'
ICONS_DIR_NAME = 'icons'


def getProgramDir():
    programFile = os.path.realpath(sys.argv[0])
    return os.path.dirname(programFile)

def generateIcon(prgDir, sz):
    srcFile = os.path.join(prgDir, 'icon.svg')
    iconsDir = os.path.join(prgDir, EXT_DIR_NAME, ICONS_DIR_NAME)
    os.makedirs(iconsDir, exist_ok=True)
    destFile = os.path.join(iconsDir, '{}.png'.format(sz))
    subprocess.check_call(['inkscape', '-z', '-e', destFile,
        '-w', str(sz), '-h', str(sz), srcFile])

if __name__ == '__main__':
    prgDir = getProgramDir()
    # https://developer.chrome.com/extensions/manifest/icons
    for sz in [128, 48, 16, 32, 19, 38]:
        generateIcon(prgDir, sz)
