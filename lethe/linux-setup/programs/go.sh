#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- Go'

LATEST=go1.2.2.linux-amd64.tar.gz
PATTERN=go[0-9]*.linux-amd64.tar.gz
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://golang.org/dl/"$LATEST" \
	--delete-pattern "$PATTERN"

rm -rf "$MB_GOROOT"
tar -C "$MB_PRG_DIR" -xzf "$MB_KITS_DIR"/$PATTERN

rm -rf "$MB_GOPATH_DIR"
mkdir "$MB_GOPATH_DIR"
