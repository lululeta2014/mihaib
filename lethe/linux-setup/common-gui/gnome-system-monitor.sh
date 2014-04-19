#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


# Set the ‘last used tab’ to be ‘resources’ instead of the default ‘disks’,
# so even the first time we run the program we see the tab we want.
if [ "$MB_LSB_ID" == 'Debian' ]; then
	if [ "$MB_LSB_CN" == 'wheezy' ]; then
		true
	else
		gsettings set org.gnome.gnome-system-monitor current-tab '"resources"'
	fi
else
	# Ubuntu 14.04 and 13.10 wants an int value for this key
	gsettings set org.gnome.gnome-system-monitor current-tab 1
fi
