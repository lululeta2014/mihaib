#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- texttest'

LATEST=texttest-3.26-with-tests.zip
REL_DIR=3.26
PATTERN=texttest-*.zip
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://master.dl.sourceforge.net/project/texttest/texttest/"$REL_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

TEXTTEST_DIR="$MB_PRG_DIR"/texttest
rm -rf "$TEXTTEST_DIR"
unzip -q -W "$MB_KITS_DIR"/$PATTERN 'texttest-*/source/**' -d "$MB_PRG_DIR"
mkdir "$TEXTTEST_DIR"
mv "$MB_PRG_DIR"/texttest-*/source "$TEXTTEST_DIR"
rm -rf "$MB_PRG_DIR"/texttest-*
