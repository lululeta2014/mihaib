#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- pyroom (MihaiB fork)'
PYROOM_DIR="$MB_PRG_DIR"/pyroom
PYROOM_DIR_TMP="$PYROOM_DIR"-tmp
rm -rf "$PYROOM_DIR_TMP"
git clone https://github.com/MihaiB/fork-pyroom.git "$PYROOM_DIR_TMP"
rm -rf "$PYROOM_DIR"
mv "$PYROOM_DIR_TMP" "$PYROOM_DIR"
