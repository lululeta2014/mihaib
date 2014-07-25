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
# When using a script to clone-move-build the toolbox, either the clone or
# the move step sets the timestamp of all files to ‘right now’ then immediately
# the build starts. So to set apart the mtime of these files, we're using
# touch on both the ‘new’ files and the ‘.bak’ files, with different dates.
# This problem in this case (cloning-moving-building) is hard to reproduce,
# but has happened.
touch -d 'last month' \
	Overwrite/ExistingBakFileForce/f2.bak \
	Overwrite/ExistingBakFileForce/f4.bak
touch -d 'last week' \
	Overwrite/ExistingBakFileForce/f2 \
	Overwrite/ExistingBakFileForce/f4
texttest.py -b arg_required_but_ignored
