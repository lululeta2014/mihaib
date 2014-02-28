#! /usr/bin/env python3

import argparse
import configparser
import hashlib
import keyring
import os
import os.path
import subprocess
import sys


program_dir = os.path.dirname(sys.argv[0])
FF_PROFILE_NAME = 'gmail'


def parse_args():
    parser = argparse.ArgumentParser(description='Start Firefox')
    parser.add_argument('--use-first', action='store_true',
            help='use first username if multiple are available')
    return parser.parse_args()


def do_first_run():
    '''For manually accepting AdBlock Plus and hiding the status bar'''
    path = os.path.expanduser('~/.mb-gmail/first-run-successful')
    if not os.path.exists(path):
        subprocess.check_call(['zenity', '--info', '--title', 'Browser Gmail',
            '--text', 'Firefox first run, for AdBlock Plus'])
        subprocess.check_call(['firefox', '-P', FF_PROFILE_NAME, '-no-remote'])
        with open(path, mode='w', encoding='utf-8'):
            pass


def getProfilePath(profile_name):
    ffDir = os.path.expanduser('~/.mozilla/firefox')
    parser = configparser.ConfigParser()
    # make it case sensitive (or it will convert keys to lowercase)
    parser.optionxform = str
    parser.read(os.path.join(ffDir, 'profiles.ini'))
    for k in parser.keys():
        v = parser[k]
        if 'Name' in v and v['Name'] == profile_name:
            return os.path.join(ffDir, v['Path'])
    print('Cannot find Firefox profile', profile_name, file=sys.stderr)


def getFirefoxVersion():
    out = subprocess.check_output(['firefox', '-v']).decode('utf-8').strip()
    return out[out.rindex(' ')+1:]


def firstRunNewVersion():
    '''
    Run once if the Firefox version is different than last time for profile.

    Otherwise, every time Selenium creates a copy of the old profile, we see
    ‘checking for compatibility with this version of Firefox’.
    '''
    version = getFirefoxVersion()
    path = getProfilePath(FF_PROFILE_NAME)
    print('PATH', path)
    with open(os.path.join(path, 'prefs.js'), 'r', encoding='utf-8') as f:
        lines = f.readlines()

    versionKeys = [
            'extensions.lastPlatformVersion',
            'extensions.lastAppVersion',
            ]
    versionMismatch = False
    for line in lines:
        if versionMismatch:
            break
        line = line.strip()
        for vKey in versionKeys:
            if line.find(vKey) != -1 and line.find('"' + version + '"') == -1:
                versionMismatch = True
                break
    if versionMismatch:
        subprocess.check_call(['zenity', '--info', '--title', 'Browser Gmail',
            '--text', 'Start Firefox to update profile version'])
        subprocess.check_call(['firefox', '-P', FF_PROFILE_NAME, '-no-remote'])


def get_users():
    users = []
    path = os.path.expanduser('~/.mb-gmail/users')
    if os.path.exists(path):
        with open(path, mode='r', encoding='utf-8') as f:
            while True:
                user = f.readline()
                if not user:
                    break
                user = user.strip()
                if user:
                    users.append(user)
    return users


def get_user_or_exit(opts):
    users = get_users()
    if not users:
        subprocess.call(['zenity', '--error', '--title', 'Browser Gmail',
            '--text', 'Gmail username(s) not set'])
        sys.exit(0)

    if len(users) == 1:
        user = users[0]
    elif users and opts.use_first:
        user = users[0]
    else:
        # guard against zenity bug where double-click or Enter returns the
        # field name twice. Test with: "zenity --list --column MyCol a b"
        args = ['zenity', '--list', '--title', 'Browser Gmail',
                '--text', 'Select username', '--column', 'username',
                '--hide-header', '--separator', '≈']
        users.sort()
        args.extend(users)
        user = subprocess.check_output(args).decode('utf-8').strip()
        if user.find('≈') != -1:
            user = user[:user.find('≈')]
    if not user:
        subprocess.call(['zenity', '--error', '--title', 'Browser Gmail',
            '--text', 'Gmail username not selected'])
        sys.exit(0)

    return user


def get_pass_or_exit(user):

    def hide(text):
        h = hashlib.sha512()
        h.update(text.encode('utf-8'))
        return h.hexdigest()

    user_clear = user
    del user
    svc_name_clear = 'google'
    user_hash, svc_name_hash = hide(user_clear), hide(svc_name_clear)

    passwd = keyring.get_password(svc_name_hash, user_hash)
    if passwd is not None:
        return passwd
    passwd = subprocess.check_output(['zenity', '--password', '--title',
        'Browser Gmail ' + svc_name_clear + '/' + user_clear
        ]).decode('utf-8').strip()
    if not passwd:
        subprocess.call(['zenity', '--error', '--title', 'Browser Gmail',
            '--text', 'You did not provide a password'])
        sys.exit(0)
    keyring.set_password(svc_name_hash, user_hash, passwd)
    return passwd


if __name__ == '__main__':
    args = parse_args()
    do_first_run()
    firstRunNewVersion()
    user = get_user_or_exit(args)
    passwd = get_pass_or_exit(user)

    p = subprocess.Popen([os.path.join(os.path.abspath(program_dir), 'run.sh'),
        'firefox-gmail', 'gmail', user, '--password-via-stdin'],
        stdin=subprocess.PIPE)
    p.communicate(bytes(passwd, 'utf-8'))
    p.stdin.close()
    p.wait()
