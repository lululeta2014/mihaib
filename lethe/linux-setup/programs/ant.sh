#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- ant'

LATEST=apache-ant-1.9.3-bin.tar.bz2
PATTERN=apache-ant-*-bin.tar.bz2
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://archive.apache.org/dist//ant/binaries/"$LATEST" \
	--delete-pattern "$PATTERN"

ANT_DIR="$MB_PRG_DIR"/ant
rm -rf "$ANT_DIR"
tar -C "$MB_PRG_DIR" -xjf "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/apache-ant-* "$ANT_DIR"
