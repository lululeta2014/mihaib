import random

from autoblog import db


def retrieve(dbfile, section_data):
    max_retrieve = section_data.getint('max_retrieve', 3)
    dictfile, numbers = section_data['retriever_data'].split(';')
    # +1 to include MAX in random.randrange(MIN, MAX+1)
    range_title, range_paragraphs, range_sentences, range_words = \
            ((int(a), int(b)+1) for a, b in
                    (x.split('-') for x in numbers.split(',')))

    known_words = set()
    with open(dictfile, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line.endswith("'s"):
                continue
            known_words.add(line)
    known_words = tuple(known_words)

    articles = []
    for x in range(max_retrieve):
        title = make_sentence(known_words, range_title)[:-1]
        count = random.randrange(*range_paragraphs)
        content = '\n\n'.join(
                make_paragraph(known_words, range_sentences, range_words)
                for x in range(count))
        articles.append((title, content))

    conn = db.connect(dbfile)
    db.create_if_missing(conn, 'random')
    random.shuffle(articles)
    db.storearticles(conn, 'random', articles)
    conn.close()


def make_paragraph(known_words, range_sentences, range_words):
    'Return a paragraph respecting sentence count and word count in sentences'

    count = random.randrange(*range_sentences)
    sentences = (make_sentence(known_words, range_words) for x in range(count))
    return ' '.join(sentences)


def make_sentence(known_words, range_words):
    'make_sentence(seq, (min, max)) -> sentence with proper capitalization'

    count = random.randrange(*range_words)
    s = ' '.join(random.choice(known_words) for x in range(count))

    x = random.randrange(12)
    if x == 0:
        end = '?'
    elif x == 1:
        end = '!'
    else:
        end = '.'

    return s[0].upper() + s[1:] + end
