#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

cp "$SCRIPT_DIR"/../simpleswing/dist/simpleswing.jar "$SCRIPT_DIR"/lib/

DL_CACHE_DIR="$SCRIPT_DIR/../meta/download-cache"
"$DL_CACHE_DIR"/download-junit.sh
cp "$DL_CACHE_DIR"/junit-*.jar "$DL_CACHE_DIR"/hamcrest-core-*.jar \
	"$SCRIPT_DIR"/test/lib

ant -q -f "$SCRIPT_DIR"/build.xml
