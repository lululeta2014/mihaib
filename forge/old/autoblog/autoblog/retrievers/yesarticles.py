import pickle
import random
import urllib.request

from autoblog import db


def retrieve(dbfile, section_data):
    categories = [cat.strip()
            for cat in section_data['retriever_data'].split(',')]
    max_history = section_data.getint('max_history', 5)
    max_retrieve = section_data.getint('max_retrieve', 3)
    do_not_publish = False

    # private_data is dict(category_id => set of max_history articles)
    conn = db.connect(dbfile)
    private_data = db.getcustomdata(conn, 'yesarticles')
    if not private_data:
        private_data = {}
        do_not_publish = section_data.getboolean('publish_only_future', False)
    else:
        private_data = pickle.loads(private_data)

    articles = []
    for cat in categories:
        urls = get_urls(cat, max_retrieve)
        new_history = urls[:max_history]
        if cat in private_data:
            known_set = private_data[cat]
            urls = newer_urls(urls, known_set)
        private_data[cat] = set(new_history)

        if do_not_publish:
            continue

        articles.extend([get_article(url) for url in urls])

    db.setcustomdata(conn, 'yesarticles', pickle.dumps(private_data))
    random.shuffle(articles)
    db.storearticles(conn, 'yesarticles', articles)
    conn.close()


def newer_urls(urls, known_set):
    'Return all urls in the list up to the first in known_set'''

    result = []
    for url in urls:
        if url in known_set:
            break
        result.append(url)
    return result


def get_urls(category, max_retrieve):
    req = urllib.request.Request(
            'http://www.yesarticles.com/articles/rss/' + category + '/')
    req.add_header('User-agent', 'ua')
    found = []

    with urllib.request.urlopen(req) as r:
        while not r.closed and len(found) < max_retrieve:
            for line in r:
                line = line.decode('utf-8').strip()
                if line == '<item>':
                    break
            url = None
            for line in r:
                line = line.decode('utf-8').strip()
                if line.startswith('<link>') and line.endswith('</link>'):
                    url = line[len('<link>'):-len('</link>')]
                    break
            if url:
                found.append(url)

    return found


def get_article(url):
    '''Returns (title, content) as a tuple'''

    req = urllib.request.Request(url)
    req.add_header('User-agent', 'ua')

    title = None
    content = []

    with urllib.request.urlopen(req) as r:
        for line in r:
            line = line.decode('utf-8').strip()
            if line.startswith('<h1>') and line.endswith('</h1>'):
                title = line[len('<h1>'):-len('</h1>')]
                break

        for line in r:
            line = line.decode('utf-8').strip()
            if line.startswith('<div class="article_body"><p'):
                line = line[len('<div class="article_body">'):]
                content.append(line)
                break

        for line in r:
            line = line.decode('utf-8').strip()
            if line == '</div>':
                break
            content.append(line)

    return (title, ' '.join(content))
