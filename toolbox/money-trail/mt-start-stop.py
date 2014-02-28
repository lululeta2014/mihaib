#! /usr/bin/env python3

import argparse
import getpass
import os
import subprocess
import sys
import tempfile

program_dir = os.path.dirname(sys.argv[0])


def parse_args():
    parser = argparse.ArgumentParser(description='Start and Stop WebServer')
    parser.add_argument('sql_gpg_file', help='money-trail.sql.gpg file')
    parser.add_argument('--diff-program', default='meld',
            help='diff program, default %(default)s')
    parser.add_argument('--browser', default='xdg-open',
            help='browser command, default %(default)s')
    parser.add_argument('--port', default=8765, type=int,
            help='Port to listen on, default %(default)s')
    parser.add_argument('--singleuser', default='MoneyTrail',
            help='-singleuser arg for webserver, default %(default)s')
    return parser.parse_args()


def gpg_encrypt(infile, outfile, passphrase):
    p = subprocess.Popen(['gpg', '--passphrase-fd', '0', '-o', outfile,
        '--yes', '--batch', '-c', infile], stdin=subprocess.PIPE)
    p.communicate(passphrase.encode('utf-8'))
    retcode = p.wait()
    if retcode:
        raise Exception('Bad gpg exit code: ' + str(retcode))


def gpg_decrypt(infile, outfile, passphrase):
    p = subprocess.Popen(['gpg', '--passphrase-fd', '0', '-o', outfile,
        '--yes', '--batch', '--quiet', '--no-mdc-warning', infile],
        stdin=subprocess.PIPE)
    p.communicate(passphrase.encode('utf-8'))
    retcode = p.wait()
    if retcode:
        raise Exception('Bad gpg exit code: ' + str(retcode))


def sqlite3_dump(dbfile, outfile):
    with open(outfile, 'w', encoding='utf-8') as f:
        f.write('PRAGMA encoding = "UTF-8";\n')
        f.write('PRAGMA foreign_keys = on;\n')
        f.write('\n')
        f.flush() # otherwise f.write() ends up after what .dump produces
        subprocess.check_call(['sqlite3', dbfile, '.dump'], stdout=f)

        # .dump produces a PRAGMA foreign_keys = off
        subprocess.check_call(['sed', '-i', outfile, '-e',
            '/^PRAGMA foreign_keys=OFF;$/d'])


def start_stop_webserver(args):
    listen_addr = 'localhost:' + str(args.port)
    browser_url = 'http://' + listen_addr + '/accounts'
    subprocess.Popen([args.browser, browser_url])
    print('Starting WebServer on', browser_url)
    subprocess.check_call([os.path.join(program_dir, 'money-trail'),
        '-listen', listen_addr,
        '-singleuser', args.singleuser,
        os.path.join(program_dir, 'datadir'),
        ])


if __name__ == '__main__':
    args = parse_args()
    db_symlink = os.path.join(program_dir, 'datadir', 'db',
            args.singleuser + '.sqlite')

    with tempfile.TemporaryDirectory() as tmpdir:
        passphrase = getpass.getpass()
        oldfile = os.path.join(tmpdir, 'oldfile')
        newfile = os.path.join(tmpdir, 'newfile')
        dbfile = os.path.join(tmpdir, 'dbfile')

        gpg_decrypt(args.sql_gpg_file, oldfile, passphrase)
        subprocess.check_call(
                ['sqlite3', '-init', oldfile, '-batch', dbfile, ''])

        if os.path.lexists(db_symlink):
            os.remove(db_symlink)
        os.symlink(dbfile, db_symlink)

        start_stop_webserver(args)

        os.remove(db_symlink)
        sqlite3_dump(dbfile, newfile)
        subprocess.check_call([args.diff_program, oldfile, newfile])

        answer = input('Overwrite SQL file? [y/n] ').lower()
        while answer != 'y' and answer != 'n':
            print('Please answer Y or N')
            answer = input('Overwrite SQL file? [y/n] ').lower()
        if answer == 'y':
            print('Overwriting', args.sql_gpg_file)
            gpg_encrypt(newfile, args.sql_gpg_file, passphrase)
        else:
            print('Not changed')
