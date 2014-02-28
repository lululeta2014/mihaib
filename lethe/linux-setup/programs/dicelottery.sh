#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- DiceLottery'

LATEST=DiceLottery-1.6.1.zip
REL_DIR=1.6
PATTERN=DiceLottery-*.zip
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://master.dl.sourceforge.net/project/dicelottery/DiceLottery/"$REL_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

DICELOTTERY_DIR="$MB_PRG_DIR"/DiceLottery
rm -rf "$DICELOTTERY_DIR"
unzip -q -d "$MB_PRG_DIR" "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/DiceLottery-* "$DICELOTTERY_DIR"
