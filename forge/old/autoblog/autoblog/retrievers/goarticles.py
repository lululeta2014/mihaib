from datetime import datetime
import pickle
import random
import urllib.request

from autoblog import db


def retrieve(dbfile, section_data):
    categories = [cat.strip()
            for cat in section_data['retriever_data'].split(',')]
    max_retrieve = section_data.getint('max_retrieve', 3)
    do_not_publish = False

    # private_data is dict(category_id => timestamp of most recent article)
    conn = db.connect(dbfile)
    private_data = db.getcustomdata(conn, 'goarticles')
    if not private_data:
        private_data = {}
        do_not_publish = section_data.getboolean('publish_only_future', False)
    else:
        private_data = pickle.loads(private_data)

    articles = []
    for cat in categories:
        urls, timestamp = get_urls(cat, max_retrieve, private_data.get(cat))
        private_data[cat] = timestamp

        if do_not_publish:
            continue

        articles.extend([get_article(url) for url in urls])

    db.setcustomdata(conn, 'goarticles', pickle.dumps(private_data))
    random.shuffle(articles)
    db.storearticles(conn, 'goarticles', articles)
    conn.close()


def get_urls(category, max_retrieve, timestamp):
    'Return ([urls], newest_moment)'''
    req = urllib.request.Request('http://goarticles.com/feeds/category/' +
            category + '/recent.rss')
    req.add_header('User-agent', 'ua')
    found = []

    with urllib.request.urlopen(req) as r:
        found = []
        newest_moment = None
        while not r.closed and len(found) < max_retrieve:
            url = moment = None
            for line in r:
                if line.decode('utf-8').strip() == '<item>':
                    break
            for line in r:
                line = line.decode('utf-8').strip()
                if line.startswith('<link>') and line.endswith('</link>'):
                    url = line[len('<link>'):-len('</link>')]
                    break
            for line in r:
                line = line.decode('utf-8').strip()
                if (line.startswith('<pubDate>') and
                        line.endswith('</pubDate>')):
                    moment = line[len('<pubDate>'):-len('</pubDate>')]
                    moment = moment[moment.index(' ')+1:]
                    moment = datetime.strptime(moment, '%d %b %Y %H:%M:%S %z')
                    break

            if not url or not moment:
                break
            if not newest_moment:
                newest_moment = moment
            newest_moment = max(newest_moment, moment)

            if timestamp and moment <= timestamp:
                break

            found.append(url)

        return (found, newest_moment)


def get_article(url):
    '''Returns (title, content) as a tuple'''

    title, content = None, []
    req = urllib.request.Request(url)
    req.add_header('User-agent', 'ua')

    with urllib.request.urlopen(req) as r:
        for line in r:
            line = line.decode('utf-8').strip()
            if line.startswith('<h1'):
                i = line.index('>')+1
                j1 = line.find('&nbsp;')
                j2 = line.find('<', 1)
                if j1 == -1:
                    j1 = len(line)
                if j2 == -1:
                    j2 = len(line)
                j = min(j1, j2)
                title = line[i:j]
                break

        for line in r:
            line = line.decode('utf-8').strip()
            if line == '<div class="KonaBody">':
                break

        # add next line as content
        for line in r:
            line = line.decode('utf-8').strip()
            content.append(line)
            break

        # <h3 ...>about the author</h3>
        for line in r:
            line = line.decode('utf-8').strip()
            if line == '<h3 class="about_author">About the Author</h3>':
                line = 'About the Author'
                content.append(line)
                break

        # add next line as content
        for line in r:
            line = line.decode('utf-8').strip()
            content.append(line)
            break

    return title, ' '.join(content)
