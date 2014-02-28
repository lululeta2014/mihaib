#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`


LATEST=jsocks.jar
PATTERN="$LATEST"
download-kit \
	--file "$SCRIPT_DIR"/lib/"$LATEST" \
	--url http://master.dl.sourceforge.net/project/jsocks/"$LATEST" \
	--delete-pattern "$PATTERN"

ant -q -f "$SCRIPT_DIR"/build.xml rebuild
