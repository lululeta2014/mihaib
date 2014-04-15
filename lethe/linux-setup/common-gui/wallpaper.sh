#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"

if [ -v MB_WALLPAPER_FILE ]; then
	"$DIR"/../tools/wallpaper/set-wallpaper.py "$MB_WALLPAPER_FILE"
fi
