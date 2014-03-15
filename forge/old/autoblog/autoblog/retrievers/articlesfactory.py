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
    private_data = db.getcustomdata(conn, 'articlesfactory')
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

    db.setcustomdata(conn, 'articlesfactory', pickle.dumps(private_data))
    random.shuffle(articles)
    db.storearticles(conn, 'articlesfactory', articles)
    conn.close()


def get_urls(category, max_retrieve, timestamp):
    'Return ([urls], newest_moment)'''
    req = urllib.request.Request(
            'http://www.articlesfactory.com/rss/' + str(category) + '.xml')
    req.add_header('User-agent', 'ua')
    found = []

    with urllib.request.urlopen(req) as r:
        for line in r:
            if line.decode('iso-8859-1').strip() == '</channel>':
                break
        found = []
        newest_moment = None
        itemstart, itemend = '<item rdf:about="', '">'
        datestart, dateend = '<dc:date>', '</dc:date>'
        while not r.closed and len(found) < max_retrieve:
            url = moment = None

            for line in r:
                line = line.decode('iso-8859-1').strip()
                if line.startswith(itemstart) and line.endswith(itemend):
                    url = line[len(itemstart):-len(itemend)]
                    break

            for line in r:
                line = line.decode('iso-8859-1').strip()
                if line.startswith(datestart) and line.endswith(dateend):
                    moment = line[len(datestart):-len(dateend)]
                    moment = moment[:-3] + moment[-2:]
                    moment = datetime.strptime(moment, '%Y-%m-%dT%H:%M:%S%z')
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
        titlestart, titleend = '<title>', '</title>'
        for line in r:
            line = line.decode('iso-8859-1').strip()
            if line.startswith(titlestart) and line.endswith(titleend):
                title = line[len(titlestart):-len(titleend)]
                break

        for line in r:
            line = line.decode('iso-8859-1').strip()
            if line == '<!-- google_ad_section_start -->':
                break

        for line in r:
            line = line.decode('iso-8859-1').strip()
            if line.startswith('<p><b>'):
                content.append(line)
                break

        for line in r:
            line = line.decode('iso-8859-1').strip()
            if line == '</script>':
                break

        for line in r:
            line = line.decode('iso-8859-1').strip()
            if line == '<!-- google_ad_section_end -->':
                break
            if len(line):
                i = line.find(
                        '<img src="http://www.articlesfactory.com/pic/x.gif"')
                if i != -1:
                    j = line.find('>', i)
                    if j != -1:
                        line = line[:i] + line[j+1:]
                content.append(line)

        # put some limits in case page markup is changed
        footer = []
        found_footer_end = False
        for line in r:
            line = line.decode('iso-8859-1').strip()
            if line == '<h1>ABOUT THE AUTHOR</h1>':
                line = 'ABOUT THE AUTHOR'
            footer.append(line)
            if line == '</p>':
                found_footer_end = True
                break

        if found_footer_end and len(footer) < 30:
            content.extend(footer)

    return title, ' '.join(content)
