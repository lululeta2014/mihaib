#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- toolbox (bootstrap)'
TOOLBOX_DIR="$MB_PRG_DIR"/toolbox
TOOLBOX_DIR_TMP="$TOOLBOX_DIR"-tmp
rm -rf "$TOOLBOX_DIR_TMP"
git clone https://github.com/MihaiB/mihaib.git "$TOOLBOX_DIR_TMP"
rm -rf "$TOOLBOX_DIR"
mv "$TOOLBOX_DIR_TMP"/toolbox "$TOOLBOX_DIR"
rm -rf "$TOOLBOX_DIR_TMP"

"$TOOLBOX_DIR"/build.py --bootstrap
