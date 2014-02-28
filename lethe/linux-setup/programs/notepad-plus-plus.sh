#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- Notepad++'
# Notepad plus plus minimalist
# http://notepad-plus-plus.org/

LATEST=npp.6.4.1.bin.minimalist.7z
REL_DIR=6.4.1
PATTERN=npp.*.bin.minimalist.7z
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://download.tuxfamily.org/notepadplus/"$REL_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

NPP_DIR="$MB_PRG_DIR"/npp.minimalist
rm -rf "$NPP_DIR"
mkdir "$NPP_DIR"
7z e -o"$NPP_DIR" "$MB_KITS_DIR"/$PATTERN
cp "$SCRIPT_DIR/npp.minimalist.files/"* "$NPP_DIR"
