#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

CONF_FILE_DIR=~/.config/pyroom
THEMES_DIR=~/.local/share/pyroom/themes

mkdir -p $CONF_FILE_DIR $THEMES_DIR

cp "$DIR/pyroom.conf" $CONF_FILE_DIR
cp "$DIR/Q10.theme" $THEMES_DIR
