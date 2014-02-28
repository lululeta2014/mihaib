#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- pyroom-mihaib'
PYROOM_MIHAIB_DIR="$MB_PRG_DIR"/pyroom-mihaib
PYROOM_MIHAIB_DIR_TMP="$PYROOM_MIHAIB_DIR"-tmp
rm -rf "$PYROOM_MIHAIB_DIR_TMP"
git clone https://github.com/MihaiB/pyroom-mihaib.git "$PYROOM_MIHAIB_DIR_TMP"
rm -rf "$PYROOM_MIHAIB_DIR"
mv "$PYROOM_MIHAIB_DIR_TMP" "$PYROOM_MIHAIB_DIR"
