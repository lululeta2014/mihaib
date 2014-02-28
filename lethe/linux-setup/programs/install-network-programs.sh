#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


# now we have Java, Go, etc installed and can build the whole toolbox
"$SCRIPT_DIR"/toolbox.sh

"$SCRIPT_DIR"/pyroom-mihaib.sh

"$SCRIPT_DIR"/jfilesync-mihaib.sh
