#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

cp "$SCRIPT_DIR"/../simpleswing/dist/simpleswing.jar "$SCRIPT_DIR"/lib/

ant -q -f "$SCRIPT_DIR"/build.xml

# run texttest tests
cd "$SCRIPT_DIR"/tests
# see tests/README for the ‘touch’ commands
touch Overwrite/ExistingBakFileForce/f2
touch Overwrite/ExistingBakFileForce/f4
texttest.py -b arg_required_but_ignored
