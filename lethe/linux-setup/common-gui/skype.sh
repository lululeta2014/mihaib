#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR"/../sourceme.bash


if [ "$MB_PRIVATE_COMP" == 1 -a "$MB_MYSELF" == "true" ]; then
	rm -rf ~/.Skype
	ln -s "$MB_BYHAND_PRG_DIR"/skype-profile-"$MB_LSB_ID"-"$MB_LSB_REL" ~/.Skype
fi
