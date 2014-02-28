import sys


def printExit(*objects):
    """Print objects to stderr then exit"""
    print(*objects, file=sys.stderr)
    sys.exit(1)
