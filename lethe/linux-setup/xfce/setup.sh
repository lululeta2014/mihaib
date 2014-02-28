#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"

"$DIR/../common-gui/setup.sh"
cp "$DIR/xinitrc" ~/.config/xfce4/
"$DIR/xfce-settings.sh"
"$DIR/xfce-3-panels.sh"
