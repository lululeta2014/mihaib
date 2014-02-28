#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/sourceme.bash"


mkdir -p "$MB_DESKTOP_DIR" "$MB_PRG_DIR" "$MB_BYHAND_PRG_DIR" "$MB_KITS_DIR"
