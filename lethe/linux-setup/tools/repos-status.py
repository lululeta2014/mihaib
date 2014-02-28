#! /usr/bin/env python3

import argparse
import os, os.path
import subprocess


REPOS_ROOT_DIR = '/mnt/haven/repos'

def parseArgs():
    parser = argparse.ArgumentParser(description='Git status/pull for ' +
            REPOS_ROOT_DIR)
    parser.add_argument('action', choices=['status', 'pull'])
    return parser.parse_args()

if __name__ == '__main__':
    args = parseArgs()
    if args.action == 'status':
        cmd = ['git', 'status', '-s', '-b']
    elif args.action == 'pull':
        cmd = ['git', 'pull']
    else:
        raise ValueError('Unexpected action ' + str(args.action))

    for name in os.listdir(REPOS_ROOT_DIR):
        print(name, end='\t', flush=True)
        subprocess.check_call(cmd, cwd=os.path.join(REPOS_ROOT_DIR, name))
