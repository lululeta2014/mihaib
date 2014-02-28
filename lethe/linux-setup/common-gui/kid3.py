#! /usr/bin/env python3

import configparser
import os


if __name__ == '__main__':
    dirpath = os.path.expanduser('~/.kde/share/config')
    filepath = os.path.join(dirpath, 'kid3rc')
    os.makedirs(dirpath, exist_ok=True)

    config = configparser.ConfigParser()
    # make it case sensitive (or it will convert keys to lowercase)
    config.optionxform = str
    config.read(filepath)

    genOptStr = 'General Options'
    if genOptStr not in config:
        config[genOptStr] = {}
    genOpt = config[genOptStr]
    genOpt['ID3v2Version'] = '1'
    genOpt['PlayOnDoubleClick'] = 'true'
    genOpt['TextEncoding'] = '2'
    genOpt['TextEncodingV1'] = 'UTF-8'

    with open(filepath, 'w') as f:
        config.write(f, space_around_delimiters=False)
