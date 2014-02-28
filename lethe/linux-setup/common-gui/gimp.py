#! /usr/bin/env python3

import subprocess
import os
import os.path


# Export PNG: Uncheck all boxes, max compression → produces:
# ~/.gimp-2.8/parasiterc with content:
# (parasite "png-save-defaults" 1 17 "\60\40\60\40\60\40\60\40\60\40\60\40\60\40\60\40\71")

if __name__ == '__main__':
    v = subprocess.check_output(['gimp', '--version']).decode('utf-8')
    v = v[v.rindex(' ')+1:]
    # convert v='2.8.6' into v='2.8'
    v = '.'.join(v.split('.')[:2])
    gimpDir = os.path.expanduser('~/.gimp-' + v)
    os.makedirs(gimpDir, exist_ok=True)
    with open(os.path.join(gimpDir, 'parasiterc'), mode='w',
            encoding='utf-8') as f:
        f.write(r'(parasite "png-save-defaults" 1 17 "\60\40\60\40\60\40\60\40\60\40\60\40\60\40\60\40\71")')
