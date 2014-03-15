import pickle
import random
from urllib.parse import urljoin
import urllib.request

from autoblog import db

def retrieve(dbfile, section_data):
    categories = [int(cat.strip())
            for cat in section_data['retriever_data'].split(',')]
    max_history = section_data.getint('max_history', 5)
    max_retrieve = section_data.getint('max_retrieve', 3)
    do_not_publish = False

    # private_data is dict(category_id => set of max_history articles)
    conn = db.connect(dbfile)
    private_data = db.getcustomdata(conn, 'articlecircle')
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

    db.setcustomdata(conn, 'articlecircle', pickle.dumps(private_data))
    random.shuffle(articles)
    db.storearticles(conn, 'articlecircle', articles)
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
            'http://www.articlecircle.com/feeds.php?cat=' + str(category))
    req.add_header('User-agent', 'ua')
    found = []

    with urllib.request.urlopen(req) as r:
        # skip the channel's <link/> (ignore everything before the first item)
        item_found = False
        for line in r:
            line = line.decode('iso-8859-1').strip()
            if not item_found:
                item_found = line == '<item>'
                continue
            if line.startswith('<link>') and line.endswith('</link>'):
                found.append(line[len('<link>'):-len('</link>')])
                if len(found) == max_retrieve:
                    break

    return found


def get_print_url(url):
    '''Returns the 'print url' given the main article url.'''

    req = urllib.request.Request(url)
    req.add_header('User-agent', 'ua')

    with urllib.request.urlopen(req) as r:
        for line in r:
            line = line.decode('iso-8859-1')
            i = line.rfind('/view/printview-')
            if i == -1:
                continue
            new_url = line[i:line.index('"', i)]

    return urljoin(url, new_url)


def get_article(url):
    '''Returns (title, content) as a tuple'''

    req = urllib.request.Request(get_print_url(url))
    req.add_header('User-agent', 'ua')

    title = None
    content = []

    with urllib.request.urlopen(req) as r:
        for line in r:
            line = line.decode('windows-1252').strip()
            if not title:
                if line.startswith('<h1>') and line.endswith('</h1>'):
                    title = line[len('<h1>'):-len('</h1>')]
                continue
            # Strip header tags from <h2>'About the Author'</h2>
            if line.startswith('<h2>') and line.endswith('</h2>'):
                line = line[len('<h2>'):-len('</h2>')] + '\n'
            if line == '</div>':
                break
            content.append(line)

    return (title, ' '.join(content))
