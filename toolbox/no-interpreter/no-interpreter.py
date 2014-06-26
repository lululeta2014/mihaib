#! /usr/bin/env python3

# Run the command line arguments as one command: arg1 arg2 … argn
# Some programs (e.g. texttest) expect to use an interpreter to run a program,
# e.g. ‘python’ for .py files, ‘java -jar’ for .jar files.
# Using this program as an interpreter will just run the arguments directly
# (e.g. your .py file should then be executable and start with a #! line).

import subprocess
import sys

if __name__ == '__main__':
    # be very defensive, handle len(sys.argv) == 0
    if len(sys.argv) < 2:
        print('Usage:', sys.argv[0] if len(sys.argv) else '',
                'arg1 … argn runs command arg1 … argn', file=sys.stderr)
        sys.exit(1)

    sys.exit(subprocess.call(sys.argv[1:]))
