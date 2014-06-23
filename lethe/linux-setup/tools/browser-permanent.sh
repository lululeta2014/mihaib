#! /usr/bin/env bash
set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

# Put this somewhere permanent (e.g. ~/Desktop/browser-profiles) and run it

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

CR_BIN=chromium
if which "$CR_BIN"; then
	true
else
	CR_BIN=chromium-browser
fi

"$CR_BIN" --user-data-dir="$DIR"/`lsb_release -cs`-profile/
