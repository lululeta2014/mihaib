#! /usr/bin/env python3

import argparse
import os, os.path
import subprocess
import sys


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.abspath(program_dir), 'util')
sys.path.append(util_dir)

from util import check_sourceme_bash_or_exit
check_sourceme_bash_or_exit(os.path.abspath(program_dir))

REPOS_ROOT_DIR = os.getenv('MB_REPOS_DIR')
repoNames = [
        'DiceLottery',
        'forge',
        'JEncConv',
        'jfilesync-mihaib',
        'lethe',
        'PyEncConv',
        'pyroom-mihaib',
        'static-mihaib',
        'toolbox',
        ]


def parseArgs():
    parser = argparse.ArgumentParser(description='Clone repos in ' +
            REPOS_ROOT_DIR)
    return parser.parse_args()


if __name__ == '__main__':
    parseArgs()
    os.mkdir(REPOS_ROOT_DIR)
    for name in repoNames:
        subprocess.check_call(['git', 'clone',
            'git@github.com.mb-mihaib:MihaiB/' + name + '.git'],
            cwd=REPOS_ROOT_DIR)
