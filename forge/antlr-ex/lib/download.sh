#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

download-kit \
	--url http://www.antlr3.org/download/antlr-3.5-complete.jar \
	--file "$SCRIPT_DIR"/development/antlr-3.5-complete.jar \
	--delete-pattern 'antlr-*-complete.jar'

download-kit \
	--url http://www.antlr3.org/download/antlr-runtime-3.5.jar \
	--file "$SCRIPT_DIR"/production/antlr-runtime-3.5.jar \
	--delete-pattern 'antlr-runtime-*.jar'
