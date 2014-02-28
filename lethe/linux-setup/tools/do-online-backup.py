#! /usr/bin/env python3

import argparse
import hashlib
import json
import keyring
import os
import subprocess
import sys


CFG_FILE = os.path.expanduser('~/.mb-online-backup.json')

def parseArgs():
    parser = argparse.ArgumentParser(description='''Helper which invokes the
            ‘online-backup’ tool multiple times for preconfigured items.
            Reads ''' + CFG_FILE + ''', a JSON list of profiles.
            Each profile is an object with arguments for the ‘online-backup’
            tool. The special keys ‘src’ and ‘dest’ denote positional
            arguments, the remaining key/value pairs are the optional args,
            e.g. {… "--git-branch": "my-branch" …}.''')
    parser.add_argument('--force', action='store_true', help='''Do the work.
            Nothing happens without this flag.''')
    return parser.parse_args()


def getAllProfiles():
    with open(CFG_FILE, encoding='utf-8') as f:
        return json.load(f)

def getArgs(profile):
    args = []
    posList = ['src', 'dest']
    for pos in posList:
        args.append(profile[pos])
    posSet = set(posList)
    for k, v in profile.items():
        if k not in posSet:
            # convert everything, e.g. numbers, to strings
            args.extend((str(k), str(v)))
    return args

def getPassword(profile):

    def hide(text):
        h = hashlib.sha512()
        h.update(text.encode('utf-8'))
        return h.hexdigest()

    if profile['dest'] == 'appengine':
        svc_name_clear = 'google'
        user_clear = profile['--appengine-email']
    else:
        raise ValueError('Unknown destination "' + profile['dest'] + '"')
    svc_name_hash, user_hash = hide(svc_name_clear), hide(user_clear)
    passwd = keyring.get_password(svc_name_hash, user_hash)
    if passwd is not None:
        return passwd
    passwd = subprocess.check_output(['zenity', '--password', '--title',
        'Online Backup ' + svc_name_clear + '/' + user_clear
        ]).decode('utf-8').strip()
    if not passwd:
        subprocess.call(['zenity', '--error', '--title', 'Online Backup',
            '--text', 'You did not provide a password'])
        raise Exception('Password not provided')
    else:
        keyring.set_password(svc_name_hash, user_hash, passwd)
    return passwd

def needsPassword(profile):
    if profile['dest'] == 'appengine':
        return True
    elif profile['dest'] in {'git', 'github'}:
        return False
    else:
        raise ValueError('Unknown destination "' + profile['dest'] + '"')

def backup(profile):
    args = ['online-backup', '--passwd-via-stdin-not-tty']
    args.extend(getArgs(profile))
    withPasswd = needsPassword(profile)
    if withPasswd:
        passwd = getPassword(profile)
    p = subprocess.Popen(args, stdin=subprocess.PIPE)
    if withPasswd:
        p.communicate(bytes(passwd, 'utf-8'))
    if p.wait():
        raise Exception('Backup failed for '
                + profile['src'] + ' → ' + profile['dest'])


if __name__ == '__main__':
    args = parseArgs()
    if not args.force:
        print('Doing nothing without ‘--force’.')
        sys.exit()

    profiles = getAllProfiles()
    for profile in profiles:
        print(profile['src'] + ' → ' + profile['dest'])
        backup(profile)
