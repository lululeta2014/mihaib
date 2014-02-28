#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR"/../sourceme.bash

rm -rf ~/.checkgmail
mkdir ~/.checkgmail

if [ -v MB_CHECKGMAIL_USERS_FILE ]; then
	# this works without crashing even if the file doesn't exist
	cat "$MB_CHECKGMAIL_USERS_FILE" 2>/dev/null | \
		while read profile; do
			# the line may have args for checkgmail-extra.py,
			# e.g. ‘username’ or ‘username -p’.
			"$DIR"/checkgmail-extra.py $profile
		done
fi
