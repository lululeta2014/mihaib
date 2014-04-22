#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR"/../sourceme.bash

rm -rf ~/.mb-gmail
if [ -v MB_BROWSER_GMAIL_USERS_FILE ]; then
	if [ -e "$MB_BROWSER_GMAIL_USERS_FILE" ]; then
		mkdir ~/.mb-gmail
		cp "$MB_BROWSER_GMAIL_USERS_FILE" ~/.mb-gmail/users
	fi
fi
