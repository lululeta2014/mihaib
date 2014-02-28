#! /usr/bin/env python3
# File name has underscores so it can be imported as a module by the test code.

import argparse
import os
import os.path


def parseArgs():
    p = argparse.ArgumentParser(description='''Remove entries from the PATH
            enviroment variable and print the resulting PATH to stdout.
            To be removed, a PATH entry and one of your provided arguments
            must be identical after being passed through os.path.normpath().

            The remaining PATH entries are written to stdout unchanged.
            ''')
    p.add_argument('entries', nargs='+', help='Entries to remove from PATH')
    return p.parse_args()


def discardEntries(pathItems, toDiscard):
    """Discard matching items and return the remaining list.

    pathItems and toDiscard are lists. An item in pathItems matches one in
    toDiscard if they are identical after passing through os.path.normpath().
    """
    toDiscard = {os.path.normpath(x) for x in toDiscard}
    return [x for x in pathItems if os.path.normpath(x) not in toDiscard]


if __name__ == '__main__':
    args = parseArgs()
    kept = discardEntries(os.getenv('PATH').split(os.pathsep), args.entries)
    print(os.pathsep.join(kept))
