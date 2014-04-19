#! /usr/bin/env python3

import argparse
import os
import os.path
import subprocess
import sys


def parseArgs():
    p = argparse.ArgumentParser(description='Set GNOME3 and XFCE wallpaper')
    p.add_argument('image', help='Path to image file')
    p.add_argument('--resize', choices=['scaled', 'zoomed'],
            help='how to resize the wallpaper')
    p.add_argument('--black-bg', action='store_true',
            help='Set the background color to black.')
    args = p.parse_args()
    args.image = os.path.abspath(args.image)
    return args

def getLsbInfo():
    '''Returns ID, release, codename from lsb_release.'''
    def getInfo(s):
        i = s.index(':')
        return s[i+1:]
    result = []
    for arg in ['-i', '-r', '-c']:
        line = (subprocess.check_output(['lsb_release', arg])
                .decode('utf-8').strip())
        result.append(getInfo(line))
    return result
lsb_id, lsb_rel, lsb_cn = getLsbInfo()

xfceResizeDict = {
    'scaled': '4',
    'zoomed': '5',
}

# Possible values are:
# "none", "wallpaper", "centered", "scaled", "stretched", "zoom", "spanned"
gnomeResizeDict = {
    'scaled': 'scaled',
    'zoomed': 'zoom',
}

def setXfce(args):
    if (lsb_id == 'Debian' or (lsb_id + '-' + lsb_rel) == 'Ubuntu-13.10'):
        subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
            '-p', '/backdrop/screen0/monitor0/image-path', '-t', 'string',
            '-s', args.image, '-n'])
        if args.resize:
            subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
                '-p', '/backdrop/screen0/monitor0/image-style', '-t', 'int',
                '-s', xfceResizeDict[args.resize], '-n'])
        if args.black_bg:
            # solid color
            subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
                '-p', '/backdrop/screen0/monitor0/color-style', '-t', 'int',
                '-s', '0', '-n'])
            subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
                '-p', '/backdrop/screen0/monitor0/color1', '-n',
                '-t', 'uint', '-s', '0',
                '-t', 'uint', '-s', '0',
                '-t', 'uint', '-s', '0',
                '-t', 'uint', '-s', '65535'])
    else:
        subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
            '-p', '/backdrop/screen0/monitorLVDS/workspace0/last-image',
            '-t', 'string', '-s', args.image, '-n'])
        if args.resize:
            subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
                '-p', '/backdrop/screen0/monitorLVDS/workspace0/image-style',
                '-t', 'int', '-s', xfceResizeDict[args.resize], '-n'])
        if args.black_bg:
            # solid color
            subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
                '-p', '/backdrop/screen0/monitorLVDS/workspace0/color-style',
                '-t', 'int', '-s', '0', '-n'])
            subprocess.check_call(['xfconf-query', '-c', 'xfce4-desktop',
                '-p', '/backdrop/screen0/monitorLVDS/workspace0/color1', '-n',
                '-t', 'uint', '-s', '0',
                '-t', 'uint', '-s', '0',
                '-t', 'uint', '-s', '0',
                '-t', 'uint', '-s', '65535'])

def setGnomeFallback(args):
    subprocess.check_call(['gsettings', 'set',
        'org.gnome.desktop.background', 'picture-uri',
        repr('file://' + args.image)])
    if args.resize:
        subprocess.check_call(['gsettings', 'set',
            'org.gnome.desktop.background', 'picture-options',
            repr(gnomeResizeDict[args.resize])])
    if args.black_bg:
        subprocess.check_call(['gsettings', 'set',
            'org.gnome.desktop.background', 'primary-color',
            repr('#000000')])


if __name__ == '__main__':
    args = parseArgs()
    setXfce(args)
    setGnomeFallback(args)
