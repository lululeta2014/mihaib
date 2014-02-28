#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- jfilesync-mihaib'
JFS_MIHAIB_DIR="$MB_PRG_DIR"/jfilesync-mihaib
JFS_MIHAIB_DIR_TMP="$JFS_MIHAIB_DIR"-tmp
rm -rf "$JFS_MIHAIB_DIR_TMP"
git clone https://github.com/MihaiB/jfilesync-mihaib.git "$JFS_MIHAIB_DIR_TMP"
rm -rf "$JFS_MIHAIB_DIR"
mv "$JFS_MIHAIB_DIR_TMP" "$JFS_MIHAIB_DIR"
ant -q -f "$JFS_MIHAIB_DIR"/build.xml
