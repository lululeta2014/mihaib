import xmlrpc.client

from autoblog import db
from autoblog.getblogpass import getblogpass

def publish(dbfile, blog_data):
    endpoint, user = blog_data.split(';')
    passwd = getblogpass('wordpress/' + blog_data)

    conn = db.connect(dbfile)
    result = db.remove_article(conn)
    db.close(conn)
    if not result:
        return
    title, content = result

    serverproxy = xmlrpc.client.ServerProxy(endpoint)
    serverproxy.metaWeblog.newPost(0, user, passwd,
            {'title': title, 'description': content}, True)
