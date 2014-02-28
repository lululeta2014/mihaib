#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

download-kit \
	--url http://sourceforge.net/projects/simpleswing/files/simpleswing-1.1.2.jar/download \
	--file "$SCRIPT_DIR"/lib/simpleswing.jar \
	--delete-pattern 'simpleswing*.jar'

ant -q -f "$SCRIPT_DIR"/build.xml rebuild-all
