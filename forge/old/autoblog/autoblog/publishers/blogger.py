import subprocess
import sys

from autoblog import db
from autoblog.getblogpass import getblogpass
from autoblog.constants import post_to_blogger_bin

def publish(dbfile, blog_data):
    blogid, user = blog_data.split(';')
    passwd = getblogpass('blogger/' + blog_data)

    conn = db.connect(dbfile)
    result = db.remove_article(conn)
    db.close(conn)
    if not result:
        return
    title, content = result

    proc = subprocess.Popen([post_to_blogger_bin, user, passwd, blogid],
            stdin=subprocess.PIPE)
    proc.communicate(input=(title + '\n' + content).encode('utf-8'))
    if proc.returncode:
        raise Exception('Posting to blogger failed with returncode ' +
                str(proc.returncode))
