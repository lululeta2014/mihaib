#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

# required by gnome3-panel.py, but also save time when every script sources it
source "$DIR/../sourceme.bash"

"$DIR/../common-gui/setup.sh"
"$DIR/gnome3-settings.sh"
"$DIR/gnome3-panel.py"
