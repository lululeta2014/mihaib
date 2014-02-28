#! /usr/bin/env python3

import ast
import subprocess
import sys


def change_visible_columns():
    '''Removes 'genre' from the visible columns list.

    Prints a warning if unknown columns are visible.
    '''

    known_cols = {'album', 'artist', 'duration', 'genre',
            'post-time', 'track-number'}
    cols_to_remove = {'genre'}

    stdout = subprocess.check_output(['gsettings', 'get',
        'org.gnome.rhythmbox.sources', 'visible-columns'])
    visible_cols = set(ast.literal_eval(stdout.decode('utf-8')))

    unexpected_cols = visible_cols.difference(known_cols)
    if unexpected_cols:
        print('Rhythmbox has unexpected columns visible',
                unexpected_cols, file=sys.stderr)
        print('You might want to update this script', file=sys.stderr)

    subprocess.check_call(['gsettings', 'set',
        'org.gnome.rhythmbox.sources', 'visible-columns',
        str(sorted(visible_cols.difference(cols_to_remove)))])


if __name__ == '__main__':
    change_visible_columns()
    subprocess.check_call(['gsettings', 'set',
        'org.gnome.rhythmbox.podcast', 'download-interval', 'manual'])
    subprocess.check_call(['gsettings', 'set',
        'org.gnome.rhythmbox.rhythmdb', 'monitor-library', 'false'])
