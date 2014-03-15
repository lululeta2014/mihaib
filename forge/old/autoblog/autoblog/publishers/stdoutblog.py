from autoblog import db

def publish(dbfile, blog_data):
    result = db.remove_article(db.connect(dbfile))
    if not result:
        return

    title, content = result
    print(title)
    print()
    print(content)
    print('-' * 79)
    print()
    print()
