#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
source "$DIR/../sourceme.bash"


if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	TERM_CONFIG_DIR=~/.config/Terminal/terminalrc
elif [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	TERM_CONFIG_DIR=~/.config/Terminal/
else
	TERM_CONFIG_DIR=~/.config/xfce4/terminal/
fi
mkdir -p "$TERM_CONFIG_DIR"
cp "$DIR/terminalrc" "$TERM_CONFIG_DIR"
