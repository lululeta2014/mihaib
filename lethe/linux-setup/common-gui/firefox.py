#! /usr/bin/env python3

import argparse
import configparser
import os
import os.path
import subprocess
import sys
import xml.dom.minidom


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)), 'util')
sys.path.append(util_dir)

from util import check_sourceme_bash_or_exit, dconf_write


# check sourceme.bash now
check_sourceme_bash_or_exit(os.path.dirname(os.path.abspath(program_dir)))

private_computer = os.getenv("MB_PRIVATE_COMP") == "1"


# We don't need to set ‘always use private browsing mode’ because Firefox
# can delete everything (history, cache) on exit.

# Selecting the ‘remember browsing history’ checkbox on the main page means
# you can auto-complete to a URL you've already visited since starting the
# browser (if you unselect the checkbox it won't autocomplete when you type)
# but selecting the 'Browsing History' checkbox under 'Clear history when
# Firefox closes' means it will be erased when you close the browser.

# This is the locked down setup for a non-private computer
prefs_js_lines = [
        # Home Page
        'user_pref("browser.startup.homepage", ' +
            '"https://encrypted.google.com/");',

        # Close downloads window when downloads complete
        'user_pref("browser.download.manager.closeWhenDone", true);',

        # Download Location
        'user_pref("browser.download.dir", "' +
            os.path.expanduser('~/Desktop') + '");',
        'user_pref("browser.download.folderList", 0);',

        # Default encoding
        'user_pref("intl.charset.default", "UTF-8");',

        # Do Not Track header
        'user_pref("privacy.donottrackheader.enabled", true);',

        # Do not accept third party cookies
        'user_pref("network.cookie.cookieBehavior", 1);',
        # Clear cookies when I exit Firefox dropdown
        'user_pref("network.cookie.lifetimePolicy", 2);',

        # FIX for Cookie Settings
        # Without this fix, the "clear cookies" checkboxes don't get checked!
        # I have no idea if the 'cookie lifetimePolicy' makes that a non-issue
        # (and if so, for all cookies or just for new cookies?) but man, I want
        # to see those checkboxes checked!
        # There are 2 settings: clearOnShutdown.cookies in Preferences,
        # cpd.cookies in Clear Recent History.
        # When editing by hand, no lines appear in prefs.js if you choose to
        # delete cookies (if you choose to keep them, 2 lines appear settings
        # those prefs to ‘false’).
        # When running ‘firefox’ instead of ‘firefox -silent’ to create a new
        # profile, a few extra lines are put in prefs.js.
        # Without these lines added to our automated script, the first time
        # you run firefox it will import/figure out preferences and the
        # ‘cookies’ checkboxes will be UNCHECKED.
        # (note: only the second line seems to be needed, leaving both though).
        'user_pref("network.cookie.prefsMigrated", true);',
        'user_pref("privacy.sanitize.migrateFx3Prefs", true);',

        # Clear history when firefox closes
        'user_pref("privacy.sanitize.sanitizeOnShutdown", true);',
        'user_pref("privacy.sanitize.didShutdownSanitize", true);',

        # Note: which lines appear in the prefs.js file MIGHT depend on both
        # your 'normal' settings (e.g. remember browsing history) and your
        # settings under 'Clear history when Firefox closes'
        # (e.g. browsing history). So changing one but not changing the other
        # can make a line appear/disappear from the prefs file.

        # Clear when Firefox closes: offline website data, site preferences
        'user_pref("privacy.clearOnShutdown.offlineApps", true);',
        'user_pref("privacy.clearOnShutdown.siteSettings", true);',

        # Don't remember passwords (unsafe until you set a Master Password)
        'user_pref("signon.rememberSignons", false);',

        # Disable smooth scrolling
        'user_pref("general.smoothScroll", false);',

        # Don't submit crash reports: TODO check if this works
        'user_pref("services.sync.nextSync", 0);',

        # Shift+F7 turns on 'caret browsing'; don't warn about it
        'user_pref("accessibility.warn_on_browsewithcaret", false);',

        # Don't warn when closing multiple tabs
        'user_pref("browser.tabs.warnOnClose", false);',

        # Tools → Clear Recent History, select Forever and click all boxes
        'user_pref("privacy.sanitize.timeSpan", 0);',
        'user_pref("privacy.cpd.offlineApps", true);',
        'user_pref("privacy.cpd.siteSettings", true);',

        # Warn when viewing a page with some unencrypted info
        # and keep the 'Alert me whenever…" checkbox pre-checked
        'user_pref("security.warn_viewing_mixed.show_once", false);',

        # Don't prompt when starting private browsing
        'user_pref("browser.privatebrowsing.dont_prompt_on_enter", true);',

        # Disable Firefox Health Report
        'user_pref("datareporting.healthreport.uploadEnabled", false);',
        # I suppose: mark that we've seen the ‘Choose what you share’
        # notification, which appears some time after Firefox creates a new
        # profile.
        'user_pref("datareporting.healthreport.service.firstRun", true);',

        # Mozilla Firefox is slow to start… don't tell me again
        'user_pref("browser.slowStartup.notificationDisabled", true);',

        # Hide the New Tab Page
        'user_pref("browser.newtabpage.enabled", false);',
]

# On a private computer, remember browsing and form&search histories
# by unchecking them under ‘Clear history when Firefox closes’
if private_computer:
    prefs_js_lines.extend([
        'user_pref("privacy.clearOnShutdown.formdata", false);',
        'user_pref("privacy.clearOnShutdown.history", false);',
    ])

# putting this inside a function just so the variables are limited to its scope
# instead of being global
def setDpi():
    """Set dpi config option only if dpi > standard 96"""
    stdDpi, dpi = 96, os.getenv('MB_DPI')
    if dpi:
        dpi = int(dpi)
        if dpi <= stdDpi:
            return
        prefs_js_lines.append(
            'user_pref("layout.css.devPixelsPerPx", "{:.4f}");'
            .format(dpi/stdDpi)
        )
setDpi()


def parse_args():
    parser = argparse.ArgumentParser(
            description='DELETE Firefox dir and setup new profile')
    parser.add_argument('-f', '--force', action='store_true',
            help='Proceed without prompting user for confirmation')
    return parser.parse_args()


def get_firefox_dir():
    return os.path.expanduser('~/.mozilla/firefox')


def get_inifile_path():
    return os.path.join(get_firefox_dir(), 'profiles.ini')


def get_profile_path(profile_dirname):
    return os.path.join(get_firefox_dir(), profile_dirname)


def create_extra_profile(profile_name):
    subprocess.check_call(['firefox', '-CreateProfile', profile_name])
    # create prefs.js and localstore.rdf in extra profile
    subprocess.check_call(['firefox', '-P', profile_name,
        '-no-remote', '-silent'])


def set_prefs(profile_dirname):
    prefs_js_path = os.path.join(get_profile_path(profile_dirname), 'prefs.js')
    with open(prefs_js_path, 'a', encoding='utf-8') as f:
        f.write('\n'.join(prefs_js_lines) + '\n')


def set_window_size_and_position(profile_dirname):
    'Sets the window size and position in localstore.rdf'

    xmlfile = os.path.join(get_profile_path(profile_dirname), 'localstore.rdf')

    doc = xml.dom.minidom.parse(xmlfile)
    e = doc.documentElement

    found = False
    for x in e.childNodes:
        if x.nodeType == x.ELEMENT_NODE and x.tagName == 'RDF:Description':
            if (x.getAttribute('RDF:about') ==
                    'chrome://browser/content/browser.xul#main-window'):
                found = True
                break

    # if we create the profile with 'firefox -silent', the .rdf file is empty
    if not found:
        x = doc.createElement('RDF:Description')
        x.setAttribute('RDF:about',
                    'chrome://browser/content/browser.xul#main-window');
        e.appendChild(x)

    x.setAttribute('width', os.getenv('MB_BROWSER_WIDTH'))
    x.setAttribute('height', os.getenv('MB_BROWSER_HEIGHT'))
    x.setAttribute('screenX', '0')
    x.setAttribute('screenY', '0')

    # if the .rdf file was empty, we must add some items
    if not found:
        c1Found = False
        for c1 in e.childNodes:
            if c1.nodeType == c1.ELEMENT_NODE and \
                    c1.tagName == 'RDF:Description':
                if (c1.getAttribute('RDF:about') ==
                        'chrome://browser/content/browser.xul'):
                    c1Found = True
                    break
        if not c1Found:
            c1 = doc.createElement('RDF:Description')
            c1.setAttribute('RDF:about','chrome://browser/content/browser.xul')
            e.appendChild(c1)

        c2Found = False
        for c2 in c1.childNodes:
            if (c2.nodeType == c2.ELEMENT_NODE and
                    c2.tagName == 'NC:persist'):
                if (c2.getAttribute('RDF:resource') ==
                        'chrome://browser/content/browser.xul#main-window'):
                    c2Found = True
                    break
        if not c2Found:
            c2 = doc.createElement('NC:persist')
            c2.setAttribute('RDF:resource',
                    'chrome://browser/content/browser.xul#main-window')
            c1.appendChild(c2)


    with open(xmlfile, 'w', encoding='utf-8') as f:
        doc.writexml(f)


def hide_menu_bar(profile_dirname):
    'Tries to obtain right-click->Hide menu bar. Not sure how good this is.'

    xmlfile = os.path.join(get_profile_path(profile_dirname), 'localstore.rdf')

    doc = xml.dom.minidom.parse(xmlfile)
    e = doc.documentElement

    x = doc.createElement('RDF:Description')
    x.setAttribute('RDF:about',
            'chrome://browser/content/browser.xul#toolbar-menubar')
    x.setAttribute('autohide', 'true')
    e.appendChild(x)

    for c in e.childNodes:
        if c.nodeType == c.ELEMENT_NODE and c.tagName == 'RDF:Description':
            if (c.getAttribute('RDF:about') ==
                    'chrome://browser/content/browser.xul'):
                x = doc.createElement('NC:persist')
                x.setAttribute('RDF:resource',
                        'chrome://browser/content/browser.xul#toolbar-menubar')
                c.appendChild(x)
                break

    with open(xmlfile, 'w', encoding='utf-8') as f:
        doc.writexml(f)


def get_adblock_plus_download_location():
    return os.path.join('/tmp', 'abp-' + os.getenv('MB_WHOAMI') + '.xpi')


def download_adblock_plus():
    'Downloads adblock plus, returns boolean indicating success.'

    url = 'https://addons.mozilla.org/firefox/downloads/latest/' + \
            'adblock-plus/addon-adblock-plus-latest.xpi'
    tmp_xpi = get_adblock_plus_download_location()
    if subprocess.call(['wget', '-nv', url, '-O', tmp_xpi]) != 0:
        print('could not download Adblock Plus', file=sys.stderr)
        return False
    return True


def install_adblock_plus(profile_dirname):
    'Downloads and installs adblock plus, quits of no network.'

    extensions_dir = os.path.join(get_profile_path(profile_dirname),
            'extensions')
    os.makedirs(extensions_dir, exist_ok=True)
    tmp_xpi = get_adblock_plus_download_location()
    xpi_destination = os.path.join(
            extensions_dir, '{d10d0bf8-f5b5-c8b4-a8b2-2b9879e08c5d}.xpi')
    subprocess.check_call(['cp', tmp_xpi, xpi_destination])

    # And that's it. NOT working on either FF.10 in Wheezy or FF.15 in Ubuntu.
    # Might have worked in FF.14 in Ubuntu.
    return

    # We might need this
    subprocess.call(['firefox', '-silent'])
    subprocess.call([
        os.path.join(
            os.path.expanduser(os.getenv('MB_TOOLS_DEST')),
            'browser-drive', 'run.sh'),
        'firefox', get_profile_path(profile_dirname)])


def disableCrashReporter():
    parser = configparser.ConfigParser()
    # make it case sensitive (or it will convert keys to lowercase)
    parser.optionxform = str
    os.makedirs(os.path.join(get_firefox_dir(), 'Crash Reports'))
    parser.read(os.path.join(get_firefox_dir(), 'Crash Reports',
        'crashreporter.ini'))
    if 'Crash Reporter' not in parser:
        parser['Crash Reporter'] = {}
    parser['Crash Reporter']['SubmitReport'] = '0'
    with open(os.path.join(get_firefox_dir(), 'Crash Reports',
        'crashreporter.ini'), 'w', encoding='utf-8') as f:
        parser.write(f, space_around_delimiters=False)


if __name__ == '__main__':
    args = parse_args()

    if not args.force:
        choice = input('Make sure Firefox is closed [Y/n] ')
        if not 'Y'.startswith(choice.upper()):
            print("Aborted by user")
            sys.exit(0)
    subprocess.check_call(['rm', '-rf', get_firefox_dir()])
    disableCrashReporter()

    # create default profile
    subprocess.check_call(['firefox', '-silent'])
    create_extra_profile(os.getenv('MB_BROWSER_ALT_PROFILE'))
    create_extra_profile('gmail')

    # xul-ext-adblock-plus
    has_adblock_plus = False if os.getenv('MB_LSB_ID') == 'Debian' \
            else download_adblock_plus()

    parser = configparser.ConfigParser()
    # make it case sensitive (or it will convert keys to lowercase)
    parser.optionxform = str
    parser.read(get_inifile_path())
    profile_keys = [k for k in parser.keys() if k.startswith('Profile')]
    for profile_path in map(get_profile_path,
            (parser[pkey]['Path'] for pkey in profile_keys)):
        set_prefs(profile_path)
        set_window_size_and_position(profile_path)
        hide_menu_bar(profile_path)
        if has_adblock_plus:
            install_adblock_plus(profile_path)

    # Firefox 10 in Debian Wheezy doesn't mark a ‘Default’ profile
    # Firefox 15 in Ubuntu 12.04 does, so this code might not be needed
    # at some point
    parser['Profile0']['Default'] = '1'
    with open(get_inifile_path(), 'w', encoding='utf-8') as f:
        parser.write(f, space_around_delimiters=False)

    # Turn off ‘Prompt integration for any website’
    if os.getenv('MB_LSB_ID') == 'Ubuntu':
        dconf_write('/com/canonical/unity/webapps/integration-allowed', False)
