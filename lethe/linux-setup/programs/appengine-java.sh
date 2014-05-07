#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- Google App Engine (Java)'

LATEST=appengine-java-sdk-1.9.4.zip
PATTERN=appengine-java-sdk-*.zip
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url https://commondatastorage.googleapis.com/appengine-sdks/featured/"$LATEST" \
	--delete-pattern "$PATTERN"

GAE_DIR="$MB_PRG_DIR"/appengine-java-sdk
rm -rf "$GAE_DIR"
unzip -q -d "$MB_PRG_DIR" "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/appengine-java-sdk-* "$MB_PRG_DIR"/appengine-java-sdk
