#! /usr/bin/env python3

import ast
import os
import sys


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)), 'util')
sys.path.append(util_dir)

from util import gsettings_get, gsettings_set, toGvariant

from util import check_sourceme_bash_or_exit
check_sourceme_bash_or_exit(os.path.dirname(os.path.abspath(program_dir)))


def setup_draw_spaces_plugin():
    plugins = ast.literal_eval(
            gsettings_get('org.gnome.gedit.plugins', 'active-plugins'))

    plgName = 'drawspaces'
    if plgName not in plugins:
        plugins.append(plgName)
        gsettings_set('org.gnome.gedit.plugins', 'active-plugins', plugins)

    if os.getenv('MB_LSB_ID') == 'Debian':
        if os.getenv('MB_LSB_CN') == 'wheezy':
            # untick View -> Show White Space
            gsettings_set('org.gnome.gedit.plugins.drawspaces', 'enable', False)
        else:
            # draw spaces, but only if they are trailing
            items = ['tab', 'space', 'trailing']
            gsettings_set('org.gnome.gedit.plugins.drawspaces', 'draw-spaces',
                    items)
    else:
        # untick View -> Show White Space
        gsettings_set('org.gnome.gedit.plugins.drawspaces', 'enable', False)


def setup_window_size():
    schema, key = 'org.gnome.gedit.state.window', 'size'
    default = (650, 500)
    target = (740, 500)
    found_str = gsettings_get(schema, key)
    if found_str not in map(toGvariant, (default, target)):
        print('Gedit window has unexpected size', found_str,
                'You may want to update this script', file=sys.stderr)
    gsettings_set(schema, key, target)


if __name__ == '__main__':
    gedit_editor_settings = {
            'display-right-margin': True,
            'highlight-current-line': True,
            'bracket-matching': True,
            'auto-indent': True,
            'create-backup-copy': False,
            }
    for k, v in gedit_editor_settings.items():
        gsettings_set('org.gnome.gedit.preferences.editor', k, v)

    setup_window_size()
    setup_draw_spaces_plugin()
