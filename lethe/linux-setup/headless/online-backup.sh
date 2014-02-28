#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR"/../sourceme.bash

rm -rf ~/.mb-online-backup.json
if [ -v MB_ONLINE_BACKUP_FILE ]; then
	if [ -e "$MB_ONLINE_BACKUP_FILE" ]; then
		cp "$MB_ONLINE_BACKUP_FILE" ~/.mb-online-backup.json
	fi
fi
