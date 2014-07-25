#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
source "$DIR/sourceme.bash"


if [ -v MB_ALT_DPI ]; then
	echo -n "Set alternative ($MB_ALT_DPI) DPI ? [y/N] "
	read OK
	if [ "$OK" == "y" -o "$OK" == "Y" ]; then
		MB_DPI="$MB_ALT_DPI"
	fi
	unset OK
fi


if [ "$DESKTOP_SESSION" == "gnome-fallback" ]; then
	"$DIR"/gnome3/setup.sh
elif [ "$DESKTOP_SESSION" == "xfce" ]; then
	"$DIR"/xfce/setup.sh
else
	echo Unknown desktop session "$DESKTOP_SESSION"
	exit 0
fi
