#! /usr/bin/env python3

import configparser
import os
import shutil


def writeOpts(filepath, opts):
    """Overwrite config file with the provided options.

    We're not loading the existing file first because ConfigParser complains
    about a missing section header (and it looks like there's also a byte-order
    mark) at the start of vlcrc.

    opts = {sectionName: items, …}
    items = {optionName: optionVal, …}
    """
    config = configparser.ConfigParser()
    # make it case sensitive (or it will convert keys to lowercase)
    config.optionxform = str

    for sectionName, items in opts.items():
        if sectionName not in config:
            config[sectionName] = {}
        section = config[sectionName]
        for k, v in items.items():
            section[k] = v

    with open(filepath, 'w', encoding='utf-8') as f:
        config.write(f, space_around_delimiters=False)


if __name__ == '__main__':
    dirpath = os.path.expanduser('~/.config/vlc')
    os.makedirs(dirpath, exist_ok=True)
    shutil.rmtree(dirpath)
    os.makedirs(dirpath)

    writeOpts(os.path.join(dirpath, 'vlc-qt-interface.conf'),
            {'General': {'IsFirstRun': '0'}})

    writeOpts(os.path.join(dirpath, 'vlcrc'),
            {'qt4': {'qt-privacy-ask': '0'}})
