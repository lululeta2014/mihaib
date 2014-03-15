#! /usr/bin/env python3

import configparser
from datetime import datetime
import os
import subprocess
import sys
import traceback

from autoblog.constants import blog_root, config_file_name, db_file_name, \
        create_tables_file
from autoblog import db
from autoblog.publishers import stdoutblog, wordpress, blogger
from autoblog.retrievers import articlecircle, articlesfactory, ezinearticles,\
        yesarticles, goarticles, publisharticle, randomretriever
from autoblog import singleinstance


if __name__ == '__main__':
    if not singleinstance.singleinstance():
        # another instance is already running
        print(datetime.now().replace(microsecond=0),
                'Another instance is already running, exiting',
                file=sys.stderr)
        sys.exit(0)

    blogs = [name for name in os.listdir(blog_root)
            if os.path.isdir(os.path.join(blog_root, name))]
    for blog in blogs:
        try:
            dbfile = os.path.join(blog_root, blog, db_file_name)
            conn = db.connect(dbfile)
            db.executescript(conn, create_tables_file)

            config = configparser.ConfigParser()
            config.read(os.path.join(blog_root, blog, config_file_name))

            if config.getboolean('DEFAULT', 'skip', fallback=False):
                print(datetime.now().replace(microsecond=0), blog,
                        'skipping', file=sys.stderr)
                continue

            hours_between_runs = config['DEFAULT']['hours_between_runs']
            if not db.can_proceed(conn, hours_between_runs):
                print(datetime.now().replace(microsecond=0), blog,
                        'no-op, too soon after last run', file=sys.stderr)
                continue

            blog_type = config['DEFAULT']['blog_type']

            for section in config.sections():
                retrieve = None
                if section == 'articlecircle':
                    retrieve = articlecircle.retrieve
                elif section == 'articlesfactory':
                    retrieve = articlesfactory.retrieve
                elif section == 'ezinearticles':
                    retrieve = ezinearticles.retrieve
                elif section == 'yesarticles':
                    retrieve = yesarticles.retrieve
                elif section == 'goarticles':
                    retrieve = goarticles.retrieve
                elif section == 'publisharticle':
                    retrieve = publisharticle.retrieve
                elif section == 'random':
                    retrieve = randomretriever.retrieve
                else:
                    print('Unknown retriever "' + section +
                            '" for blog "' + blog + '"', file=sys.stderr)

                if retrieve:
                    try:
                        retrieve(dbfile, config[section])
                    except ValueError as err:
                        # superclass of UnicodeDecodeError
                        print(datetime.now().replace(microsecond=0),
                                'Exception caught for blog', blog, 'section',
                                section, file=sys.stderr)
                        print(err, file=sys.stderr)

            publish = None
            if blog_type == 'stdout':
                publish = stdoutblog.publish
            elif blog_type == 'wordpress':
                publish = wordpress.publish
            elif blog_type == 'blogger':
                publish = blogger.publish
            else:
                print('Unknown blog type "' + blog_type +
                        '" for blog "' + blog + '"', file=sys.stderr)

            if publish:
                publish(dbfile, config['DEFAULT']['blog_data'])

            for section in config.sections():
                db.trim_article_queue(conn, section,
                        int(config[section]['max_queue_size']))

            db.close(conn)
        except Exception:
            print(datetime.now().replace(microsecond=0),
                    'Exception caught for blog', blog, file=sys.stderr)
            traceback.print_exc()
