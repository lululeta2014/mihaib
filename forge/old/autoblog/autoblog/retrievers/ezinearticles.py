from datetime import datetime
import pickle
import random
from urllib.parse import urljoin
import urllib.request
import urllib.error

from autoblog import db


def retrieve(dbfile, section_data):
    categories = [cat.strip()
            for cat in section_data['retriever_data'].split(',')]
    max_retrieve = section_data.getint('max_retrieve', 3)
    do_not_publish = False

    # private_data is dict(category_id => timestamp of most recent article)
    conn = db.connect(dbfile)
    private_data = db.getcustomdata(conn, 'ezinearticles')
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

        articles.extend([get_article_with_redirects(url) for url in urls])

    db.setcustomdata(conn, 'ezinearticles', pickle.dumps(private_data))
    random.shuffle(articles)
    db.storearticles(conn, 'ezinearticles', articles)
    conn.close()


def get_urls(category, max_retrieve, timestamp):
    'Return ([urls], newest_moment)'''
    req = urllib.request.Request('http://feeds.ezinearticles.com/category/' +
            category + '.xml')
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


def get_article_with_redirects(url):
    'Returns get_article_content(url) but handles several redirect attempts.'

    for attempts in range(3):
        try:
            return get_article_content(url)
        except urllib.error.HTTPError as e:
            if e.getcode() == 301 or e.getcode() == 302:
                # urllib doesn't auto-redirect for this website, throwing
                # this exception instead; redirect by hand
                url = urljoin(url, e.geturl())
            else:
                raise

    # if we didn't succeed yet, call again to throw exception
    return get_article_content(url)


def get_article_content(url):
    '''Returns (title, content) as a tuple'''

    title, content = None, []
    req = urllib.request.Request(url)
    req.add_header('User-agent', 'ua')

    with urllib.request.urlopen(req) as r:
        titlestart, titleend = '<title>', '</title>'
        for line in r:
            line = line.decode('utf-8').strip()
            if line.startswith(titlestart) and line.endswith(titleend):
                title = line[len(titlestart):-len(titleend)]
                break

        for line in r:
            line = line.decode('utf-8').strip()
            if line == '<div id="article-content">':
                break

        # add next line as content
        for line in r:
            line = line.decode('utf-8').strip()
            if line.endswith('</div>'):
                line = line[:-len('</div>')]
            content.append(line)
            break

        for line in r:
            line = line.decode('utf-8').strip()
            if line == '<div id="article-resource">':
                break

        # add next line as content
        for line in r:
            line = line.decode('utf-8').strip()
            if line.endswith('</div>'):
                line = line[:-len('</div>')]
            content.append(line)
            break

        # what follows are:
        # - blank lines
        # - article source over 3 lines
        # - blank lines
        source = []
        for line in r:
            line = line.decode('utf-8').strip()
            if len(line):
                source.append(line)
            else:
                if source:
                    break

        if len(source) <= 3:
            content.extend(source)

    return title, ' '.join(content)
