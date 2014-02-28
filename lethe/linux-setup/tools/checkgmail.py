#! /usr/bin/env python3

from collections import OrderedDict
import json
import os.path
import subprocess


if __name__ == '__main__':
    with open(os.path.expanduser('~/.checkgmail/accounts.json'),
            encoding='utf-8') as f:
        users = json.load(f, object_pairs_hook=OrderedDict)
        for u, enabled in users.items():
            if enabled:
                subprocess.Popen(['checkgmail-gnome-keyring', '-no_cookies',
                    '-profile=' + u])
