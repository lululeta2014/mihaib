#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- jfilesync (MihaiB fork)'
JFS_DIR="$MB_PRG_DIR"/jfilesync
JFS_DIR_TMP="$JFS_DIR"-tmp
rm -rf "$JFS_DIR_TMP"
git clone https://github.com/MihaiB/fork-jfilesync.git "$JFS_DIR_TMP"
rm -rf "$JFS_DIR"
mv "$JFS_DIR_TMP" "$JFS_DIR"
ant -q -f "$JFS_DIR"/build.xml
