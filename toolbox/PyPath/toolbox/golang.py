import os, os.path

from toolbox.util import printExit


def getGOPATH():
    """Return GOPATH env var if single dir or exit with error."""
    gopath = os.getenv('GOPATH')
    if not gopath:
        printExit('GOPATH env var is missing or empty')
    if gopath.find(':') != -1:
        printExit('GOPATH contains multiple entries')
    return gopath
