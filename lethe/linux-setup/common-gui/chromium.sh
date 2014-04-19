#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"

if [ "$MB_LSB_ID" == "Debian" ]; then
	CHROMIUM_BIN='chromium'
else
	CHROMIUM_BIN='chromium-browser'
fi

FORCE_FLAG=''
if [ $# == 1 ]; then
	# can't use [$#==1 -a $1=='--force'] because $1 is unbound if $# is 0
	if [ $1 == '--force' ]; then
		FORCE_FLAG='--force'
	fi
fi

rm -rf ~/.config/chromium ~/.cache/chromium
"$DIR"/chromium.py $FORCE_FLAG ~/.config/chromium \
	--window-width "$MB_BROWSER_WIDTH" \
	--window-height "$MB_BROWSER_HEIGHT" \
	--chromium-bin "$CHROMIUM_BIN"

rm -rf ~/.config/chromium-"$MB_BROWSER_ALT_PROFILE" \
	~/.cache/chromium-"$MB_BROWSER_ALT_PROFILE"
"$DIR"/chromium.py $FORCE_FLAG ~/.config/chromium-"$MB_BROWSER_ALT_PROFILE" \
	--enable-translate \
	--window-width "$MB_BROWSER_WIDTH" \
	--window-height "$MB_BROWSER_HEIGHT" \
	--chromium-bin "$CHROMIUM_BIN"
