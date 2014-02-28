#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR"/../sourceme.bash


"$DIR"/../programs/repo-selenium.sh
rm -rf "$MB_TOOLS_DEST"
cp -r "$DIR"/../tools "$MB_TOOLS_DEST"

# This copy operation will change the timestaps of files, which may incorrectly
# tell ‘ant’ everything is up-to-date. Clean to force a build next time.
ant -q -f "$MB_TOOLS_DEST/browser-drive/build.xml" clean
