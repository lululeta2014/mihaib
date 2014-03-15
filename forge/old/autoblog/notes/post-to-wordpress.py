import xmlrpc.client
s = smlrpc.client.ServerProxy('https://myblog.wordpress.com/xmlrpc.php')
s.metaWeblog.newPost(0, 'user', 'pass',
        {'title':'My Title', 'description':'Hello <!--more--> there.'}, True)
