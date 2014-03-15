import subprocess
from autoblog.constants import keyring_bin

def getblogpass(blog):
    out_bytes = subprocess.check_output([keyring_bin, 'autoblog', blog])
    return out_bytes.decode('utf-8').rstrip('\r\n')
