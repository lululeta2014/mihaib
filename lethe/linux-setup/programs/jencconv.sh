#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- JEncConv'

LATEST=JEncConv-2.1.1.zip
REL_DIR=2.1
PATTERN=JEncConv-*.zip
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://master.dl.sourceforge.net/project/jencconv/JEncConv/"$REL_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

JENCCONV_DIR="$MB_PRG_DIR"/JEncConv
rm -rf "$JENCCONV_DIR"
unzip -q -d "$MB_PRG_DIR" "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/JEncConv-* "$JENCCONV_DIR"
