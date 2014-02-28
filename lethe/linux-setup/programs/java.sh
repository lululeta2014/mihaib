#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- Java'

SRV_DIR=7u51-b13
LATEST=jdk-7u51-linux-x64.tar.gz
PATTERN='jdk-7*.tar.gz'
download-kit \
	--delete-pattern "$PATTERN" \
	--url http://download.oracle.com/otn-pub/java/jdk/"$SRV_DIR"/"$LATEST" \
	--file "$MB_KITS_DIR"/"$LATEST" \
	-- --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com"

JAVA_DIR="$MB_PRG_DIR"/jdk
rm -rf "$JAVA_DIR"
tar -C "$MB_PRG_DIR" -xzf "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/jdk1.7.* "$JAVA_DIR"
