#! /usr/bin/env python3

import argparse
from collections import OrderedDict
import json
import os
import subprocess
import sys


program_dir = os.path.dirname(sys.argv[0])
closeme_html = os.path.join(program_dir, 'closeme.html')

homepage = 'https://encrypted.google.com/'


def parseArgs():
    parser = argparse.ArgumentParser(
            description='DELETE and set up Chromium profile')
    parser.add_argument('user_data_dir', help='''‘--user-data-dir’ arg to pass
            to Chromium, e.g. ~/.config/chromium''')
    parser.add_argument('--save-passwords', action='store_true',
            help='let browser offer to save passwords')
    parser.add_argument('--enable-translate', action='store_true',
            help='let browser offer to translate webpages')
    parser.add_argument('--window-width', type=int, default=1024,
            metavar='width',
            help='browser window width, default %(default)s pixels')
    parser.add_argument('--window-height', type=int, default=768,
            metavar='height',
            help='browser window height, default %(default)s pixels')
    parser.add_argument('-f', '--force', action='store_true',
            help='proceed without reminding user to close open browsers')
    parser.add_argument('--chromium-bin', default='chromium',
            help='chromium binary (eg chromium-browser) default ‘%(default)s’')
    return parser.parse_args()


def loadJson(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f, object_pairs_hook=OrderedDict)

def writeJson(data, path):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=3, separators=(',', ': '))


def setDeep(obj, path, val):
    '''Set val at dot-separated path in obj.

    Non-existent intermediate paths are set to empty objects before descending.
    '''
    path = path.split('.')
    for key in path[:-1]:
        if key not in obj:
            obj[key] = {}
        obj = obj[key]
    obj[path[-1]] = val

def setMultiDeep(obj, thingsToSet):
    'Wraps setDeep to set multiple path-keys to their associated values.'
    for path, val in thingsToSet.items():
        setDeep(obj, path, val)


class Profile():

    def __init__(self, args):
        self.userDataDir = args.user_data_dir
        self.savePasswords = args.save_passwords
        self.enableTranslate = args.enable_translate
        self.windowWidth = args.window_width
        self.windowHeight = args.window_height
        self.chromiumBin = args.chromium_bin
        self.ensureProfileExists()
        self.prefs = loadJson(self.getPrefsPath())
        self.localState = loadJson(self.getLocalStatePath())

    def ensureProfileExists(self):
        subprocess.check_call([self.chromiumBin,
            '--user-data-dir=' + self.userDataDir, closeme_html])

    def getPrefsPath(self):
        return os.path.join(self.userDataDir, 'Default', 'Preferences')

    def getLocalStatePath(self):
        return os.path.join(self.userDataDir, 'Local State')

    def modify(self):
        raise NotImplementedError('Implement me and change prefs fields')

    def write(self):
        writeJson(self.prefs, self.getPrefsPath())
        writeJson(self.localState, self.getLocalStatePath())

    def run(self):
        self.modify()
        self.write()


class ProfileV30(Profile):

    def __init__(self, args):
        super().__init__(args)

    def modify(self):
        prefs, localState = {}, {}

        prefs.update({
            ### Browser window size and placement
            'browser.window_placement.left': 0,
            'browser.window_placement.top': 0,
            'browser.window_placement.right': self.windowWidth,
            'browser.window_placement.bottom': self.windowHeight,
            'browser.window_placement.maximized': False,

            ### Don't ask to set Chromium as default browser (needed on Ubuntu)
            'browser.check_default_browser': False,

            ### Show ‘Most Visited’ (1024) not ‘Apps’ (2048) in New (empty) Tab
            'ntp.shown_page': 1024,

            ### On Startup
            # Open a specific page or set of pages
            'session.restore_on_startup': 4,
            # set pages
            'session.urls_to_restore_on_startup': [homepage],

            ### Appearance
            # Use Classic Theme
            'extensions.theme.use_system': False,
            # Show Home button
            'browser.show_home_button': True,
            # → Change home button from ‘New Tab Page’ to ‘open this page’
            'homepage_is_newtabpage': False,
            # → set homepage
            'homepage': homepage,
            # uncheck ‘always show the bookmarks bar’
            'bookmark_bar.show_on_all_tabs': False,
            # uncheck Use system title bar and borders (needed on Ubuntu)
            'browser.custom_chrome_frame': True,

            ### Privacy

            ### Privacy → Content settings
            # Keep local data only until I quit my browser
            'profile.default_content_settings.cookies': 4,
            # Block third-party cookies
            # (this appears to be turned on and the key missing by default
            # in Debian Jessie Chromium v30)
            'profile.block_third_party_cookies': True,
            # Do not allow any site to handle protocols
            'custom_handlers.enabled': False,
            # Do not allow any site to track my physical location
            'profile.default_content_settings.geolocation': 2,
            # Do not allow any site to show desktop notifications
            'profile.default_content_settings.notifications': 2,
            # Do not allow any sites to access my camera and microphone
            'profile.default_content_settings.media-stream': 2,
            # (unsandboxed plug-in access)
            # do not allow any sites to use a plugin to access my computer
            'profile.default_content_settings.ppapi-broker': 2,
            # (automatic downloads)
            # do not allow any site to download multiple files automatically
            'profile.default_content_settings.multiple-automatic-downloads': 2,

            ### Privacy → Clear Browsing Data
            # the beginning of time
            'browser.clear_data.time_period': 4,
            # Clear browsing history
            'browser.clear_data.browsing_history': True,
            # Clear download history
            'browser.clear_data.download_history': True,
            # Delete cookies and other site and plug-in data
            'browser.clear_data.cookies': True,
            # Empty the cache
            'browser.clear_data.cache': True,
            # Clear saved Autofill form data
            'browser.clear_data.form_data': True,
            # Clear data from hosted apps
            'browser.clear_data.hosted_apps_data': True,

            ### Privacy
            # uncheck use a web service to help resolve navigation errors
            'alternate_error_pages.enabled': False,
            # uncheck use a prediction service to help complete searches & URLs
            'search.suggest_enabled': False,
            # uncheck predict network actions to improve page load performance
            'dns_prefetching.enabled': False,
            # Send a Do Not Track request with your browsing traffic
            'enable_do_not_track': True,

            ### Passwords an Forms
            # uncheck Enable Autofill to fill out web forms in a single click
            'autofill.enabled': False,
            # Offer to save passwords
            'profile.password_manager_enabled': self.savePasswords,

            ### Web Content
            # Font size; leave the default
            #'webkit.webprefs.default_font_size': 16,
            # Standard font
            'webkit.webprefs.fonts.standard.Zyyy': 'DejaVu Serif',
            # Serif font
            'webkit.webprefs.fonts.serif.Zyyy': 'DejaVu Serif',
            # Sans-serif font
            'webkit.webprefs.fonts.sansserif.Zyyy': 'DejaVu Sans',
            # Fixed width
            'webkit.webprefs.fonts.fixed.Zyyy': 'DejaVu Sans Mono',

            # Minimum font size
            'webkit.webprefs.minimum_font_size': 11,
            'webkit.webprefs.minimum_logical_font_size': 11,
            # This gets set to various values is the JSON; let's choose a size:
            'webkit.webprefs.default_fixed_font_size': 13,
            # Encoding
            'intl.charset_default': 'UTF-8',

            ### Languages
            # Offer to translate pages that aren't in a language I read
            'translate.enabled': self.enableTranslate,

            ### Downloads
            # Download location
            'download.default_directory': os.path.expanduser('~/Desktop'),
            # (the setting isn't picked up without ‘directory_upgrade: true’)
            'download.directory_upgrade': True,
            'savefile.default_directory': os.path.expanduser('~/Desktop'),
        })
        localState.update({
            ### System
            # uncheck Continue running background apps when Chromium is closed
            'background_mode.enabled': False,
        })

        setMultiDeep(self.prefs, prefs)
        setMultiDeep(self.localState, localState)


class ProfileV32(Profile):

    def __init__(self, args):
        super().__init__(args)

    def modify(self):
        prefs, localState = {}, {}

        prefs.update({
            ### Browser window size and placement
            'browser.window_placement.left': 0,
            'browser.window_placement.top': 0,
            'browser.window_placement.right': self.windowWidth,
            'browser.window_placement.bottom': self.windowHeight,
            'browser.window_placement.maximized': False,

            ### Don't ask to set Chromium as default browser (needed on Ubuntu)
            'browser.check_default_browser': False,

            ### Show ‘Most Visited’ (1024) not ‘Apps’ (2048) in New (empty) Tab
            'ntp.shown_page': 1024,

            ### On Startup
            # Open a specific page or set of pages
            'session.restore_on_startup': 4,
            # set pages
            'session.startup_urls': [homepage],

            ### Appearance
            # Use Classic Theme
            'extensions.theme.use_system': False,
            # Show Home button
            'browser.show_home_button': True,
            # → Change home button from ‘New Tab Page’ to ‘open this page’
            'homepage_is_newtabpage': False,
            # → set homepage
            'homepage': homepage,
            # uncheck ‘always show the bookmarks bar’
            'bookmark_bar.show_on_all_tabs': False,
            # uncheck Use system title bar and borders (needed on Ubuntu)
            'browser.custom_chrome_frame': True,

            ### Privacy

            ### Privacy → Content settings
            # Keep local data only until I quit my browser
            'profile.default_content_settings.cookies': 4,
            # Block third-party cookies
            # (this appears to be turned on and the key missing by default
            # in Debian Jessie Chromium v30)
            'profile.block_third_party_cookies': True,
            # Do not allow any site to handle protocols
            'custom_handlers.enabled': False,
            # Do not allow any site to track my physical location
            'profile.default_content_settings.geolocation': 2,
            # Do not allow any site to show desktop notifications
            'profile.default_content_settings.notifications': 2,
            # Do not allow any sites to access my camera and microphone
            'profile.default_content_settings.media-stream': 2,
            # (unsandboxed plug-in access)
            # do not allow any sites to use a plugin to access my computer
            'profile.default_content_settings.ppapi-broker': 2,
            # (automatic downloads)
            # do not allow any site to download multiple files automatically
            'profile.default_content_settings.multiple-automatic-downloads': 2,

            ### Privacy → Clear Browsing Data
            # the beginning of time
            'browser.clear_data.time_period': 4,
            # Clear browsing history
            'browser.clear_data.browsing_history': True,
            # Clear download history
            'browser.clear_data.download_history': True,
            # Delete cookies and other site and plug-in data
            'browser.clear_data.cookies': True,
            # Empty the cache
            'browser.clear_data.cache': True,
            # Clear saved Autofill form data
            'browser.clear_data.form_data': True,
            # Clear data from hosted apps
            'browser.clear_data.hosted_apps_data': True,

            ### Privacy
            # uncheck use a web service to help resolve navigation errors
            'alternate_error_pages.enabled': False,
            # uncheck use a prediction service to help complete searches & URLs
            'search.suggest_enabled': False,
            # uncheck predict network actions to improve page load performance
            'dns_prefetching.enabled': False,
            # Send a Do Not Track request with your browsing traffic
            'enable_do_not_track': True,

            ### Passwords an Forms
            # uncheck Enable Autofill to fill out web forms in a single click
            'autofill.enabled': False,
            # Offer to save passwords
            'profile.password_manager_enabled': self.savePasswords,

            ### Web Content
            # Font size; leave the default
            #'webkit.webprefs.default_font_size': 16,
            # Standard font
            'webkit.webprefs.fonts.standard.Zyyy': 'DejaVu Serif',
            # Serif font
            'webkit.webprefs.fonts.serif.Zyyy': 'DejaVu Serif',
            # Sans-serif font
            'webkit.webprefs.fonts.sansserif.Zyyy': 'DejaVu Sans',
            # Fixed width
            'webkit.webprefs.fonts.fixed.Zyyy': 'DejaVu Sans Mono',

            # Minimum font size
            'webkit.webprefs.minimum_font_size': 11,
            'webkit.webprefs.minimum_logical_font_size': 11,
            # This gets set to various values is the JSON; let's choose a size:
            'webkit.webprefs.default_fixed_font_size': 13,
            # Encoding
            'intl.charset_default': 'UTF-8',

            ### Languages
            # Offer to translate pages that aren't in a language I read
            'translate.enabled': self.enableTranslate,

            ### Downloads
            # Download location
            'download.default_directory': os.path.expanduser('~/Desktop'),
            # (the setting isn't picked up without ‘directory_upgrade: true’)
            'download.directory_upgrade': True,
            'savefile.default_directory': os.path.expanduser('~/Desktop'),
        })
        localState.update({
            ### System
            # uncheck Continue running background apps when Chromium is closed
            'background_mode.enabled': False,
        })

        setMultiDeep(self.prefs, prefs)
        setMultiDeep(self.localState, localState)

    def writeSqlite(self):
        """This is really fragile.

        Comment it out if it breaks.
        """
        sqlitefile = os.path.join(self.userDataDir, 'Default', 'Local Storage',
                'chrome-devtools_devtools_0.localstorage')
        # ensure the sqlite file exists
        subprocess.check_call([
            os.path.join(program_dir, '../tools/browser-drive/run.sh'),
            'chromiumdevtools', self.userDataDir])
        p = subprocess.Popen(['sqlite3', sqlitefile], stdin=subprocess.PIPE)
        command='''INSERT INTO ItemTable values('cacheDisabled', 'true');'''
        p.communicate(bytes(command, 'utf-8'))
        p.stdin.close()
        if p.wait():
            print('Error setting ‘Disable cache while DevTools are open’',
                    'via sqlite3', file=sys.stdout)

    def write(self):
        super().write()
        self.writeSqlite()


def getChromiumVersion(chromiumBin):
    out = subprocess.check_output([chromiumBin, '--version']).decode('utf-8')
    out = out[out.index(' '):]
    out = out[:out.index('.')]
    return int(out)


def dispatch(args):
    v = getChromiumVersion(args.chromium_bin)
    if v < 30:
        raise ValueError('Unsupported Chromium version: ' + str(v))
    elif v < 32:
        chosen = ProfileV30
    else:
        chosen = ProfileV32
    chosen(args).run()


if __name__ == '__main__':
    args = parseArgs()
    if not args.force:
        choice = input('Make sure Chromium is closed [Y/n] ')
        if not 'Y'.startswith(choice.upper()):
            print("Cancelled by user")
            sys.exit()
    dispatch(args)
