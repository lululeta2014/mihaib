#! /usr/bin/env python3

import argparse
import json
import os
import os.path
import sys

from collections import OrderedDict


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)), 'util')
sys.path.append(util_dir)

from util import check_sourceme_bash_or_exit


def parse_args():
    parser = argparse.ArgumentParser(
            description='Chromium preferences without browser automation')

    parser.add_argument('chromium_dir',
            help='Chromium dir, e.g. ~/.config/chromium')

    return parser.parse_args()


def set_preferences(args):
    prefs_file = os.path.join(args.chromium_dir, 'Default', 'Preferences')
    with open(prefs_file, 'r', encoding='utf-8') as f:
        prefs_data = json.load(f, object_pairs_hook=OrderedDict)

    # Don't ask to set as Default Browser
    if 'browser' not in prefs_data:
        prefs_data['browser'] = {}
    prefs_data['browser']['check_default_browser'] = False

    # replace whatever 'window_placement' there was
    prefs_data['browser']['window_placement'] = {}
    window_placement = prefs_data['browser']['window_placement']
    window_placement['maximized'] = False
    top = 0
    left = 0
    bottom = top + int(os.getenv('MB_BROWSER_HEIGHT'))
    right = left + int(os.getenv('MB_BROWSER_WIDTH'))
    window_placement['top'] = top
    window_placement['left'] = left
    window_placement['bottom'] = bottom
    window_placement['right'] = right

    # Download directory
    if 'download' not in prefs_data:
        prefs_data['download'] = {}
    prefs_data['download']['default_directory'] = \
            os.path.join(os.getenv('HOME'), 'Desktop')

    # Hide bookmarks bar
    if 'bookmark_bar' not in prefs_data:
        prefs_data['bookmark_bar'] = {}
    prefs_data['bookmark_bar']['show_on_all_tabs'] = False

    # Show 'Most visited' (1024) not 'Apps' (2048) in new (empty) tab
    # This looks fragile.
    if 'ntp' not in prefs_data:
        prefs_data['ntp'] = {}
    prefs_data['ntp']['shown_page'] = 1024

    # Font sizes
    if 'webkit' not in prefs_data:
        prefs_data['webkit'] = {}
    if 'webprefs' not in prefs_data['webkit']:
        prefs_data['webkit']['webprefs'] = {}
    prefs_data['webkit']['webprefs']['minimum_font_size'] = 11
    prefs_data['webkit']['webprefs']['minimum_logical_font_size'] = 11
    prefs_data['webkit']['webprefs']['default_font_size'] = 16
    prefs_data['webkit']['webprefs']['default_fixed_font_size'] = 13

    with open(prefs_file, 'w', encoding='utf-8') as f:
        # Our pretty-print differs from chromium's (most notably lists)
        # so the file will get reformatted after starting&stopping Chromium.
        # However, if we want to diff before&after this script, this helps.
        json.dump(prefs_data, f, indent=3, separators=(',', ': '))


if __name__ == '__main__':
    check_sourceme_bash_or_exit(os.path.dirname(os.path.abspath(program_dir)))
    args = parse_args()
    set_preferences(args)
