#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- chromedriver'

# http://code.google.com/p/chromedriver/downloads/list
VERSION=2.9
LATEST=chromedriver_linux64_"$VERSION".zip
PATTERN='chromedriver_linux64_*.zip'
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://chromedriver.storage.googleapis.com/"$VERSION"/chromedriver_linux64.zip \
	--delete-pattern "$PATTERN"

CHROMEDRIVER_DIR="$MB_PRG_DIR"/chromedriver
rm -rf "$CHROMEDRIVER_DIR"
mkdir "$CHROMEDRIVER_DIR"
unzip -q -d "$CHROMEDRIVER_DIR" "$MB_KITS_DIR"/$PATTERN
