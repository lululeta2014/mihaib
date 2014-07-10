#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"

if [ -v MB_VBOX_VMS_DIR ]; then
	mkdir -p "$MB_VBOX_VMS_DIR"
	if which vboxmanage >/dev/null; then
		vboxmanage setproperty machinefolder "$MB_VBOX_VMS_DIR"
	else
		echo 'vboxmanage not found'
	fi
fi
