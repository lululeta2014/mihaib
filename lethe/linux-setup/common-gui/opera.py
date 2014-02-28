#! /usr/bin/env python3

# Opera's settings say "Delete new cookies when exiting Opera"
# The first time it runs, before we enforce that setting,
# some cookies get created (opera update site, homepage, speed dial sites).
# Those won't get deleted unless we do it by hand from the GUI.

# For now this seems to work: Having only the settings file in the profile dir.
# We can do it either by:
# 1) never starting Opera at all to create a profile
# 2) starting&stopping Opera, changing the settings file and saving it,
#    deleting profile/ and recreating it with just the saved settings file.
# Method 1) does not require an active GUI session (works in headless mode).

#touch "$OPERA_PROFILE_DIR"/operaprefs.ini "$OPERA_PROFILE_DIR"/pluginpath.ini

import argparse
import configparser
import os
import os.path
import subprocess
import sys


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)), 'util')
sys.path.append(util_dir)

from util import check_sourceme_bash_or_exit


# check sourceme bash NOW; opera_prefs uses ','.join([…, os.getenv(...), …])
# which will crash if the var is not defined and os.getenv() returns None.
# Makes little difference here, but we always prefer to crash not mask errors
# (e.g. by using str(os.getenv(...)) here).

check_sourceme_bash_or_exit(os.path.dirname(os.path.abspath(program_dir)))

private_computer = os.getenv('MB_PRIVATE_COMP') == "1"


opera_prefs = [
        # Don't show License Agreement dialog on first run
        ('State', 'Accept License', '1'),

        # General → Startup → Start with home page
        ('User Prefs', 'Startup Type', '2'),
        ('User Prefs', 'Show Startup Dialog', '0'),
        # General → Startup → Home page
        ('User Prefs', 'Home URL', 'https://encrypted.google.com/'),
        # General → Pop-ups → Block unwanted pop-ups
        ('User Prefs', 'Ignore Unrequested Popups', '1'),
        # General → Language → Details → Encoding to assume for pages
        ('User Prefs', 'Fallback HTML Encoding', 'utf-8'),

        # Forms → (uncheck) Enable Password Manager
        ('User Prefs', 'Enable Wand', '0'),

        # Advanced → Tabs → Cycle without showing list
        ('User Prefs', 'Window Cycle Type', '2'),
        # Advanced → Tabs → Open new tab next to active
        ('User Prefs', 'Open Page Next To Current', '1'),

        # Advanced → Browsing → Show full web address in address field
        ('User Prefs', 'Show Full URL', '1'),

        # Advanced → Notifications → (uncheck) Enable program sounds
        ('Sounds', 'Enabled', '0'),

        # Advanced → Fonts → Minimum font size (pixels)
        ('User Prefs', 'Minimum Font Size', '12'),

        # Advanced → Downloads → Download folder
        ('User Prefs', 'Download Directory', '{Home}Desktop'),
        ('Saved Settings', 'Open Dir', '{Home}Desktop'),
        ('Saved Settings', 'Save Dir', '{Home}Desktop'),

        # Advanced → History → (uncheck) Remember content on visited pages
        ('User Prefs', 'Visited Pages', '0'),
        # Advanced → History → Disk cache → Empty on exit
        ('Disk Cache', 'Empty On Exit', '1'),

        # Advanced → Cookies → Accept cookies only from the site I visit
        ('User Prefs', 'Enable Cookies', '3'),
        # Advanced → Cookies → Delete new cookies when exiting Opera
        ('User Prefs', 'Accept Cookies Session Only', '1'),

        # Advanced → Security → Ask websites not to track me
        ('Network', 'Enable Do Not Track Header', '1'),

        # Advanced → Network → (uncheck) Enable geolocation
        ('Geolocation', 'Enable geolocation', '0'),

        # Advanced → Storage → Use application cache: No; no idea what this is
        ('User Prefs', 'Strategy On Application Cache', '2'),

        # Advanced → Toolbars → (uncheck) Double-click text for context menu
        ('User Prefs', 'Automatic Select Menu', '0'),

        # Window Position
        # apparently x,y,width,height,state[2=maximized, 0=restored]
        # If you use 0,0 for x,y and you have a top panel in Gnome,
        # the window will be shown somewhere else (e.g. 2,84).
        # But for 0,1 or 0,-1 it's shown as expected at 0,24 under the panel.
        # Also, if height is too large, it gets corrected on the first run.
        # And if we place it way off to the right 2000,-1 it's corrected.
        # We want Opera at the right side of the screen.
        ('Windows', 'Browser Window', ','.join(['2000', '-1',
            os.getenv('MB_BROWSER_WIDTH'),
            os.getenv('MB_BROWSER_HEIGHT'),
            '0'])),
        ]

if not private_computer:
    opera_prefs.extend([
        # Advanced → History → Addresses: 0
        ('User Prefs', 'Max Direct History Lines', '0'),
        ('User Prefs', 'Max Global History Lines', '0'),
        ])


def parse_args():
    parser = argparse.ArgumentParser(
            description='Set Opera prefs and Java plugin path')
    parser.add_argument('--force', action='store_true',
            help='Do not ask user to confirm that Opera is closed')
    return parser.parse_args()


def user_confirm_or_exit():
    'Asks the user to confirm Opera is closed; if negative, exits the program.'

    choice = input('Make sure Opera is closed [Y/n] ')
    if choice == '' or choice.upper() == 'Y':
        return
    print('Cancelled by user')
    sys.exit(0)


def set_cfg_pref(config, section_name, key, value):
    if section_name not in config:
        config[section_name] = {}
    section = config[section_name]
    section[key] = value


def add_to_opera_ini_file(filename, preferences):
    'Add preferences to filename; items are (section_name, key, value).'

    # Some settings have no value
    config = configparser.ConfigParser(allow_no_value=True)
    # make it case sensitive (or it will convert keys to lowercase)
    config.optionxform = str

    # If we run this on a real file created by Opera (not a blank one), we need
    # to discard the first line of the file, which is outside a section.
    # This is how opera writes this .ini file
    with open(filename, 'r', encoding='utf-8') as f:
        first_line = f.readline()
        config.read_file(f)

    for (section_name, key, value) in preferences:
        set_cfg_pref(config, section_name, key, value)

    with open(filename, 'w', encoding='utf-8') as f:
        f.write(first_line)
        f.write('\n')
        config.write(f, space_around_delimiters=False)


if __name__ == '__main__':
    args = parse_args()
    if not args.force:
        user_confirm_or_exit()
    subprocess.check_call(['rm', '-rf', os.getenv('MB_OPERA_PREFS_ROOT')])

    for profile_path in (os.getenv('MB_OPERA_REG_PROF_PATH'),
            os.getenv('MB_OPERA_ALT_PROF_PATH')):
        os.makedirs(profile_path)
        prefs_path = os.path.join(profile_path, 'operaprefs.ini')
        plugin_path = os.path.join(profile_path, 'pluginpath.ini')
        subprocess.check_call(['touch', prefs_path, plugin_path])
        add_to_opera_ini_file(prefs_path, opera_prefs)
        add_to_opera_ini_file(plugin_path,
                [('Paths', '~/.mozilla/plugins', '1')])
