#! /usr/bin/env python3

import argparse
import getpass
import os
import shutil
import subprocess
import sys
import tempfile


program_dir = os.path.dirname(os.path.realpath(sys.argv[0]))


def parse_args():
    parser = argparse.ArgumentParser(description='Online backup to '
            + 'Google App Engine, GitHub or a git repository.')
    parser.add_argument('src', help='Source (directory to backup)')
    parser.add_argument('dest', choices=['appengine', 'github', 'git'],
            help='Destination (one of %(choices)s)')
    parser.add_argument('--preprocess-tool', metavar='tool',
            help='Program invoked with a copy of the source directory as its '
            + 'single argument, before uploading to the destination')
    parser.add_argument('--passwd-via-stdin-not-tty', action='store_true',
            help='''Use Python's built-in input() function instead of
                getpass.getpass() if prompting for any passwords.
                Use this when you want to pipe the password via stdin, because
                since Python 3.3.4 getpass uses the terminal and waits for you
                to type the password instead of reading it from stdin.''')

    gae_group = parser.add_argument_group('Google App Engine')
    gae_group.add_argument('--appengine-app-id', metavar='app_id')
    gae_group.add_argument('--appengine-email', metavar='email')
    gae_group.add_argument('--appengine-version', metavar='version',
            help='''Always use the same version number (e.g. 1). By overwriting
            the version marked as ‘Default’ in AppEngine you instantly replace
            what it serves and you don't consume extra storage because you
            don't store multiple versions.''')

    git_group = parser.add_argument_group('Git')
    git_group.add_argument('--git-push-url', metavar='url')
    git_group.add_argument('--git-branch', metavar='branch', default="master",
            help="Branch to push to, default %(default)s")
    git_group.add_argument('--git-commit-message', metavar='msg')
    git_group.add_argument('--git-user-name', metavar='"A U Thor"',
            default="user", help="git user.name, default %(default)s")
    git_group.add_argument('--git-user-email', metavar='author@example.com',
            default="user@host", help="git user.email, default %(default)s")

    github_group = parser.add_argument_group('GitHub',
            'Accepts all options in the Git section and additionally:')
    github_group.add_argument('--github-dns-cname', metavar='cname',
            help='optional DNS CNAME. Set your A record to the same IP as '
            + 'pages.github.com.')

    return parser.parse_args()


class Destination:

    def __init__(self, path):
        self.path = path

    def upload(self):
        raise NotImplementedError('Subclasses must implement this method')


def getDestination(args, path):
    '''Factory method for Destination from the program args'''

    if args.dest == 'appengine':
        return GoogleAppEngineDest(path, args)
    elif args.dest == 'git':
        return GitDest(path, args)
    elif args.dest == 'github':
        return GitHubDest(path, args)
    else:
        raise ValueError('Unexpected destination "' + args.dest + '"')


class GoogleAppEngineDest(Destination):

    def __init__(self, path, args):
        super().__init__(path)
        self.appid = args.appengine_app_id
        self.email = args.appengine_email
        self.version = args.appengine_version
        self.passwd_via_stdin_not_tty = args.passwd_via_stdin_not_tty
        if not self.appid or not self.email or not self.version:
            raise ValueError('Incomplete args for Google App Engine')

    def addWebInf(self):
        webinf = os.path.join(self.path, 'WEB-INF')
        os.mkdir(webinf)
        shutil.copy(os.path.join(program_dir, 'web.xml'), webinf)
        shutil.copy(os.path.join(program_dir, 'appengine-web.xml'), webinf)

    def upload(self):
        self.addWebInf()
        if self.passwd_via_stdin_not_tty:
            passwd = input('Password (via stdin not tty, as you requested): ')
        else:
            passwd = getpass.getpass()
        p = subprocess.Popen(['appcfg.sh', '--no_cookies', '--passin',
            '-A', self.appid, '-e', self.email, '-V', self.version,
            'update', self.path],
            stdin = subprocess.PIPE)
        p.communicate(passwd.encode('utf-8'))
        p.stdin.close()
        if p.wait():
            raise Exception('appcfg.sh failed')


class GitDest(Destination):

    def __init__(self, path, args):
        super().__init__(path)
        self.pushUrl, self.branch = args.git_push_url, args.git_branch
        self.commitMsg = args.git_commit_message
        self.userName, self.userEmail = args.git_user_name, args.git_user_email
        if (not self.pushUrl or not self.branch or not self.commitMsg
                or not self.userName or not self.userEmail):
            raise ValueError('Incomplete args for Git')

    def commit(self):
        subprocess.check_call(['git', 'init'], cwd=self.path)
        subprocess.check_call(['git', 'config', 'user.name', self.userName],
                cwd=self.path)
        subprocess.check_call(['git', 'config', 'user.email', self.userEmail],
                cwd=self.path)
        subprocess.check_call(['git', 'add', '.'], cwd=self.path)
        subprocess.check_call(['git', 'commit', '-m', self.commitMsg],
                cwd=self.path)

    def push(self):
        subprocess.check_call(['git', 'push', self.pushUrl,
            '+master:' + self.branch], cwd=self.path)

    def upload(self):
        self.commit()
        self.push()


class GitHubDest(GitDest):

    def __init__(self, path, args):
        super().__init__(path, args)
        self.cname = args.github_dns_cname

    def upload(self):
        if self.cname:
            with open(os.path.join(self.path, 'CNAME'), mode='x',
                    encoding='utf-8') as f:
                f.write(self.cname)
        super().upload()


if __name__ == '__main__':
    args = parse_args()
    with tempfile.TemporaryDirectory() as tmpdir:
        rootdir = shutil.copytree(args.src, os.path.join(tmpdir, 'a'))

        gitdir = os.path.join(rootdir, '.git')
        os.makedirs(gitdir, exist_ok=True)
        shutil.rmtree(gitdir)

        if args.preprocess_tool:
            subprocess.check_call([args.preprocess_tool, rootdir])

        dest = getDestination(args, rootdir)
        dest.upload()
