#!/usr/bin/env python

import argparse
import atom
import gdata
from gdata import service
import sys

def parse_args():
    parser = argparse.ArgumentParser(
            description='''Post to Blogger

            The post title and content are read from stdin.
            The first line is the title, the rest of input (until EOF) is
            the content.
            ''')
    parser.add_argument('username')
    parser.add_argument('password')
    parser.add_argument('blogid', help="You can get this from the blog's feed")
    return parser.parse_args()

if __name__ == '__main__':
    args = parse_args()

    title = sys.stdin.readline().strip()
    content = sys.stdin.read()
    if not title or not content:
        print >>sys.stderr, 'post-to-blogger err: title or content are missing'
        sys.exit(1)

    blogger = service.GDataService(args.username, args.password)
    blogger.source = 'blogger'  # This is a User-agent of your choosing
    blogger.service = 'blogger'
    blogger.server = 'www.blogger.com'
    blogger.ProgrammaticLogin()

    entry = gdata.GDataEntry()
    entry.title = atom.Title('text', title)
    entry.content = atom.Content(content_type='html', text=content)

    blogger.Post(entry, '/feeds/' + args.blogid + '/posts/default')
