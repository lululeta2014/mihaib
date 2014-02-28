#! /usr/bin/env python3

import argparse
import collections
import json
import os
import os.path
import sys


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)), 'util')
sys.path.append(util_dir)

from util import check_sourceme_bash_or_exit


def parse_args():
    parser = argparse.ArgumentParser(
            description='Add another checkgmail account')
    parser.add_argument('username', help='gmail username')
    parser.add_argument('-p', '--private', help='show popup very briefly',
            action='store_true')
    return parser.parse_args()


def write_xml(args):
    with open(os.path.expanduser('~/.checkgmail/prefs-'+args.username+'.xml'),
            'w', encoding='utf-8') as f:
        f.write('''
            <opt
                archive_as_read="0"
                atomfeed_address="mail.google.com/mail/feed/atom"
                delay="120000"
                gmail_command="firefox -P lowmen -no-remote https://gmail.com/"
                language="English"
                nomail_command=""
                notify_command=""
                popup_delay="''' + str(6000 if not args.private else 1) + '''"
                save_passwd="1"
                show_popup_delay="''' + str(6000 if
                    args.private and os.getenv('MB_PRIVATE_COMP') != '1'
                    else 250) + '''"
                time_24="0"
                user="''' + args.username + '''"
            >
                <label_delay></label_delay>
            </opt>
            ''')


def append_username(username):
    filepath = os.path.expanduser('~/.checkgmail/accounts.json')

    if os.path.exists(filepath):
        with open(filepath, encoding='utf-8') as f:
            users = json.load(f, object_pairs_hook=collections.OrderedDict)
    else:
        users = {}

    users[username] = True

    with open(filepath, mode='w', encoding='utf-8') as f:
        json.dump(users, f, indent=2)


if __name__ == '__main__':
    check_sourceme_bash_or_exit(os.path.dirname(os.path.abspath(program_dir)))
    args = parse_args()
    write_xml(args)
    append_username(args.username)
