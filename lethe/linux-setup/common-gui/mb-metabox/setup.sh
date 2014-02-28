#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`


# move this somewhere else if any other script uses any of these themes dirs
TARGET=~/.local/share/themes
LINK=~/.themes
rm -rf "$TARGET" "$LINK"
mkdir -p "$TARGET"
ln -s "$TARGET" "$LINK"

DEST="$TARGET"/Mb-Metabox/metacity-1/
mkdir -p "$DEST"
cp "$DIR"/metacity-theme-1.xml "$DEST"
