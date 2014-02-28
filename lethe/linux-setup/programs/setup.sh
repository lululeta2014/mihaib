#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


"$SCRIPT_DIR"/install-local-programs.sh

echo -n "Install additional programs over the Internet? [Y/n] "
read OK
if [ "$OK" == "" -o "$OK" == "y" -o "$OK" == "Y" ]; then
	"$SCRIPT_DIR"/install-network-programs.sh
fi
