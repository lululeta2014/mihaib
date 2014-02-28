#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


# Default applications

MIME_FILE=~/.local/share/applications/mimeapps.list
mkdir -p `dirname $MIME_FILE`
cp "$DIR/mimeapps.list" $MIME_FILE

if [ "$MB_LSB_ID" == Debian ]; then
	sed -e 's/firefox.desktop/iceweasel.desktop/' -i $MIME_FILE
fi
