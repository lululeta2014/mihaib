#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- node js'

VERSION="v0.10.26"
LATEST=node-"$VERSION"-linux-x64.tar.gz
PATTERN=node-v[0-9]*-linux-x64.tar.gz
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://nodejs.org/dist/"$VERSION"/"$LATEST" \
	--delete-pattern "$PATTERN"

NODE_DIR="$MB_PRG_DIR"/node
rm -rf "$NODE_DIR"
tar -C "$MB_PRG_DIR" -xzf "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/node-v* "$NODE_DIR"
