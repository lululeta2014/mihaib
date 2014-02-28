#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"

if [ $# == 1 ]; then
	# can't use [$#==1 -a $1=='--force'] because $1 is unbound if $# is 0
	if [ $1 == '--force' ]; then
		OK=Y
	fi
fi
if [ ! -v OK ]; then
	echo -n "Make sure Chromium is closed [Y/n] "
	read OK
fi

if [ "$OK" == "" -o "$OK" == "y" -o "$OK" == "Y" ]; then
	# You must be able to write
	# chromedriver.log in the directory you're in when running this script,
	# otherwise chromium will timeout without starting.

	rm -rf ~/.config/chromium ~/.cache/chromium
	"$MB_TOOLS_DEST"/browser-drive/run.sh chromium ~/.config/chromium
	"$DIR"/old-chromium-extra.py ~/.config/chromium

	rm -rf ~/.config/chromium-"$MB_BROWSER_ALT_PROFILE" \
		~/.cache/chromium-"$MB_BROWSER_ALT_PROFILE"
	"$MB_TOOLS_DEST"/browser-drive/run.sh chromium \
		~/.config/chromium-"$MB_BROWSER_ALT_PROFILE" \
		--enable-translate
	"$DIR"/old-chromium-extra.py ~/.config/chromium-"$MB_BROWSER_ALT_PROFILE"

	if false; then
		rm -rf ~/.config/chromium-gmail ~/.cache/chromium-gmail
		"$MB_TOOLS_DEST"/browser-drive/run.sh \
			chromium ~/.config/chromium-gmail --save-pass
		"$DIR"/old-chromium-extra.py ~/.config/chromium-gmail
	fi
else
	echo 'Cancelled by user'
fi
