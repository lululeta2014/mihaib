# http://brianwisti.blogspot.com/2009/06/python-blogger-refresh-part-1.html

# post-to-blog.py

import markdown
from xml.etree import ElementTree
from gdata import service
import gdata
import atom
import sys

class BlogPost(object):
    """A single posting for a blog owned by a Blogger account

    >>> post = BlogPost('John Smith', 'me@here.com', 'mysecretpassword')
    >>> post.body = 'This is a paragraph'
    >>> print post.body
    <p>This is a paragraph</p>
    """

    def __init__(self, author, account, password):
        self.config = {}
        self.__body = None
        self.__author = author
        self.__account = account
        self.__password = password

    def set_body(self, bodyText):
        """Stores plain text which will be used as the post body

        >>> post = BlogPost('John Smith', 'me@here.com', 'mysecretpassword')
        >>> post.set_body('This is a paragraph')
        >>>
        """
        self.__body = bodyText

    def get_body(self):
        """Access a HTML-formatted version of the post body

        >>> post = BlogPost('John Smith', 'me@here.com', 'mysecretpassword')
        >>> post.set_body('This is a paragraph')
        >>> print post.get_body()
        <p>This is a paragraph</p>
        """
        return markdown.markdown(self.__body)

    body = property(get_body, set_body)

    def parseConfig(self, configText):
        """Reads and stores the directives from the post's config header.

        >>> post = BlogPost('John Smith', 'me@here.com', 'mysecretpassword')
        >>> import os
        >>> myConfig = os.linesep.join(["key1: value1", "key2: value2"])
        >>> post.parseConfig(myConfig)
        >>> post.config['key1']
        'value1'
        >>> post.config['key2']
        'value2'
        """
        textLines = configText.splitlines()
        for line in textLines:
            key, value = line.split(': ')
            self.config[key] = value

    def parsePost(self, postText):
        """Parses the contents of a full post, including header and body.

        >>> import os
        >>> myText = os.linesep.join(["title: Test", "--", "This is a test"])
        >>> post = BlogPost('John Smith', 'me@here.com', 'mysecretpassword')
        >>> post.parsePost(myText)
        >>> print post.config['title']
        Test
        >>> print post.body
        <p>This is a test</p>
        """
        header, body = postText.split('--', 1)
        self.parseConfig(header)
        self.body = body

  def sendPost(self):
      """Log into Blogger and submit my already parsed post"""

      # Authenticate using ClientLogin
      blogger = service.GDataService(self.__account, self.__password)
      blogger.source = 'post-to-blog.py_v01.0'
      blogger.service = 'blogger'
      blogger.server = 'www.blogger.com'
      blogger.ProgrammaticLogin()

      # Get the blog ID
      query = service.Query()
      query.feed = '/feeds/default/blogs'
      feed = blogger.Get(query.ToUri())
      blog_id = feed.entry[0].GetSelfLink().href.split('/')[-1]

      # Create the entry to insert.
      entry = gdata.GDataEntry()
      entry.author.append(atom.Author(atom.Name(text=self.__author)))
      entry.title = atom.Title('xhtml', self.config['title'])
      entry.content = atom.Content(content_type='html', text=self.body)

      # Assemble labels, if any
      if 'tags' in self.config:
          tags = self.config['tags'].split(',')
          for tag in tags:
              category = atom.Category(term=tag, scheme='http://www.blogger.com/atom/ns#')
              entry.category.append(category)

      # Decide whether this is a draft.
      control = atom.Control()
      control.draft = atom.Draft(text='yes')
      entry.control = control

      # Submit it!
      blogger.Post(entry, '/feeds/' + blog_id + '/posts/default')

def runTests():
    import doctest
    doctest.testmod()

def main():
    from optparse import OptionParser
    parser = OptionParser()
    parser.add_option("-D", "--do-tests", action="store_true", dest="doTests",
                      help="Run built-in doctests")
    parser.add_option("-f", "--file", dest="filename",
                      help="Specify source file for post")
    (options, args) = parser.parse_args()

    if options.doTests:
        runTests()

    if options.filename:
        post = BlogPost('John Smith', 'me@here.com', 'mysecretpassword')
        postFile = open(options.filename).read()
        post.parsePost(postFile)
        post.sendPost()

if __name__ == '__main__':
    main()
