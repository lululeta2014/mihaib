import os

bufsize = 4096

def convert(source, src_enc, dest, dest_enc):
    '''Converts encoding; if dest is None overwrites src (after moving to .bak)

    Some exceptions thrown:
    IOError
    OSError?
    LookupError
    Exception
    '''

    if dest == None:
        bak = source + ".bak"
        if os.path.exists(bak):
            raise Exception(bak + ' exists, aborting')
        os.rename(source, bak)
        source, dest = bak, source

    if os.path.exists(dest):
            raise Exception('destination ' + dest + ' exists, aborting')

    with open(source, 'r', encoding=src_enc, newline='') as src_file:
        with open(dest, 'w', encoding=dest_enc, newline='') as dest_file:
            text = src_file.read(bufsize)
            while len(text):
                dest_file.write(text)
                text = src_file.read(bufsize)
