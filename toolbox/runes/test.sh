#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

python3 -m unittest discover "$SCRIPT_DIR"

# run texttest tests
cd "$SCRIPT_DIR"/texttest
texttest.py -b this_arg_is_required_but_ignored
