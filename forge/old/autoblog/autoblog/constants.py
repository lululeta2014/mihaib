import os, sys

# any port not assigned to a well known service
BIND_PORT = 3121

prg_dir = os.path.dirname(sys.argv[0])

utils_path = os.path.join(prg_dir, 'utils')
keyring_bin = os.path.join(utils_path, 'getkrpass.py')
post_to_blogger_bin = os.path.join(utils_path, 'post-to-blogger.py')

blog_root = os.path.join(prg_dir, 'blogs')

db_file_name = 'blog.db'
create_tables_file = os.path.join(prg_dir, 'create-tables.sql')

config_file_name = 'config.ini'
