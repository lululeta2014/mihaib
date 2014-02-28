#! /usr/bin/env python3

import argparse
import fnmatch
import os, os.path
import shutil
import subprocess
import sys
import tempfile


def parseArgs():
    p = argparse.ArgumentParser(description='''Exit if file exists.
            Otherwise download it to a temporary location, delete everything
            in the target file's directory matching pattern (if given) and
            move the newly downloaded file there.''')
    p.add_argument('-u', '--url', required=True)
    p.add_argument('-f', '--file', required=True,
            help='''(path to) file to save.''')
    p.add_argument('--delete-pattern', help='''Unix shell-style wildcard used
            to delete files in the same directory as the target file.''')
    p.add_argument('wget_args', nargs='*', help='''args for wget, e.g.
            ‘download […args…] --
            --header "Cookie: gpw_e24=http%%3A%%2F%%2Fwww.oracle.com"’.
            This is a positional (not --optional) arg because Python's argparse
            is having troube taking values starting with dashes ‘-’,
            e.g. --header.''')
    return p.parse_args()


if __name__ == '__main__':
    args = parseArgs()
    if os.path.exists(args.file):
        sys.exit()
    with tempfile.TemporaryDirectory() as tmpDir:
        # normpath converts A/B/ to A/B
        destDir, fileName = os.path.split(os.path.normpath(args.file))
        # destDir is '' for args.file=='myfile'
        destDir = destDir or '.'

        wgetCmd = ['wget', args.url, '-O', fileName]
        wgetCmd.extend(args.wget_args)
        subprocess.check_call(wgetCmd, cwd=tmpDir)

        if args.delete_pattern:
            for item in os.listdir(destDir):
                if fnmatch.fnmatch(item, args.delete_pattern):
                    # remove only succeeds on files; ok for now
                    os.remove(os.path.join(destDir, item))

        shutil.move(os.path.join(tmpDir, fileName), destDir)
