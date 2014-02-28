#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- PyEncConv'

LATEST=PyEncConv-1.0.zip
REL_DIR=1.0
PATTERN=PyEncConv-*.zip
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://master.dl.sourceforge.net/project/pyencconv/PyEncConv/"$REL_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

PYENCCONV_DIR="$MB_PRG_DIR"/PyEncConv
rm -rf "$PYENCCONV_DIR"
unzip -q -d "$MB_PRG_DIR" "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/PyEncConv-* "$PYENCCONV_DIR"
