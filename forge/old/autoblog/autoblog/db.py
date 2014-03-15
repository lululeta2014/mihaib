from contextlib import closing
import sqlite3


def connect(filename):
    '''Connects to filename and returns the connection object'''

    conn = sqlite3.connect(filename)
    # sqlite3.Row allows accessing DB results by (case insensitive) column name
    conn.row_factory = sqlite3.Row
    with closing(conn.cursor()) as cursor:
        cursor.execute('PRAGMA foreign_keys = on;')
        cursor.fetchall()
    return conn


def close(conn):
    '''Commits and closes the connection'''

    conn.commit()
    conn.close()


def executescript(conn, scriptfilename):
    with open(scriptfilename, 'r', encoding='utf-8') as f:
        contents = f.read()
        with closing(conn.cursor()) as cursor:
            cursor.executescript(contents)
            cursor.fetchall()
        conn.commit()


def create_if_missing(conn, retriever_name):
    '''Creates a retriever with the given name if not present in the DB'''

    with closing(conn.cursor()) as cursor:
        sql = 'SELECT * FROM retrievers WHERE name = :name;'
        cursor.execute(sql, {'name' : retriever_name})
        size = len(cursor.fetchall())
        assert size == 0 or size == 1, \
                'Multiple retrievers with same name "' + retriever_name + "'"
        if size == 0:
            sql = 'INSERT INTO retrievers(name) VALUES(:name);'
            cursor.execute(sql, {'name' : retriever_name})
            cursor.fetchall()
            conn.commit()


def getcustomdata(conn, retriever_name):
    '''Gets custom data for the retriever.

    The retriever is created if it doesn't exist.
    '''

    create_if_missing(conn, retriever_name)
    with closing(conn.cursor()) as cursor:
        sql = 'SELECT private_data FROM retrievers WHERE name = :name;'
        cursor.execute(sql, {'name' : retriever_name})
        results = cursor.fetchall()
    assert len(results) == 1, \
            'Expected 1 retriever named "' + name + "' got " + len(results)
    return results[0]['private_data']


def setcustomdata(conn, retriever_name, private_data):
    '''Sets private_data (should be a bytes object) for the retriever.

    The retriever is created if it doesn't exist.
    '''

    create_if_missing(conn, retriever_name)
    with closing(conn.cursor()) as cursor:
        sql = '''UPDATE retrievers SET private_data = :private_data
                WHERE name = :name'''
        cursor.execute(sql,
                {'name' : retriever_name, 'private_data' : private_data})
        cursor.fetchall()
    conn.commit()


def storearticles(conn, retriever_name, articles):
    '''Stores articles which is a list of (title, content) tuples.

    The retriever_name must already exist.'''

    with closing(conn.cursor()) as cursor:
        sql = '''INSERT INTO articles(source, title, content)
                VALUES(
                (SELECT ID FROM retrievers WHERE name = :retriever_name),
                :title, :content);'''
        data = [{'retriever_name' : retriever_name,
            'title' : title, 'content' : content}
            for (title, content) in articles]
        cursor.executemany(sql, data)
        cursor.fetchall()

    conn.commit()


def trim_article_queue(conn, retriever_name, max_len):
    '''Delete oldest (i.e. with smallest ID) articles if queue is too long.'''

    with closing(conn.cursor()) as cursor:
        sql = '''SELECT COUNT(*) FROM articles WHERE
                source = (SELECT ID FROM retrievers WHERE name = :name);'''
        cursor.execute(sql, {'name' : retriever_name})
        crt_len = cursor.fetchall()[0][0]

        # Apparently default sqlite3 is compiled without support for
        # ORDER BY in DELETE statements. Doing it in a SELECT subquery.
        if crt_len > max_len:
            sql = '''DELETE FROM articles WHERE
                    ID in (
                        SELECT ID FROM articles WHERE
                        source = (SELECT ID FROM retrievers WHERE name = :name)
                        ORDER BY ID ASC
                        LIMIT :count
                    );'''
            cursor.execute(sql,
                    {'name': retriever_name, 'count': crt_len - max_len})
            cursor.fetchall()

    conn.commit()


def remove_article(conn):
    '''DELETE the oldest article of a random source, return (title, content).

    Randomly select a source from the 'articles' table (i.e. one of the sources
    with articles available).
    Delete its oldest article and return it to the caller.
    Returns None if no articles are available.
    '''

    with closing(conn.cursor()) as cursor:
        sql = '''SELECT MIN(ID) FROM articles WHERE
                source = (SELECT DISTINCT source FROM articles
                            ORDER BY RANDOM()
                            LIMIT 1)'''
        cursor.execute(sql)
        results = cursor.fetchall()

        # results is never an empty list because of the named field 'MIN(ID)'
        # The only sqlite3.Row in the list may be {'MIN(ID)': None}
        article_id = results[0][0]
        if not article_id:
            return None

        sql = 'SELECT title, content FROM articles WHERE ID = :id;'
        cursor.execute(sql, {'id': article_id})
        [(title, content)] = cursor.fetchall()

        sql = 'DELETE FROM articles WHERE ID = :id;'
        cursor.execute(sql, {'id': article_id})
        cursor.fetchall()

    conn.commit()
    return title, content


def can_proceed(conn, hours_between_runs):
    '''Returns boolean showing if enough time has passed since the last run.

    If enough time has passed (or previous time absent from DB) the current
    date/time is stored in the DB before this function returns.
    '''

    with closing(conn.cursor()) as cursor:
        sql='''
        SELECT
        CASE
        WHEN EXISTS (SELECT moment FROM times WHERE name = 'last_execution')
        THEN
            (SELECT DATETIME() >= DATETIME(moment, :hours || ' hours')
            FROM times WHERE name = 'last_execution')
        ELSE 1
        END
        AS should_run;
        '''
        # param :hours doesn't get replaced if it's inside quotes in the SQL,
        # so we're using string concatenation: :hours || ' hours'
        cursor.execute(sql, {'hours': hours_between_runs})
        if cursor.fetchall()[0]['should_run']:
            sql = '''
            INSERT OR REPLACE INTO times(name, moment)
            VALUES('last_execution', DATETIME());'''
            cursor.execute(sql);
            cursor.fetchall();
            conn.commit();
            return True
        return False
