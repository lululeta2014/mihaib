#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

JUNIT_V=4.11
download-kit \
	--url http://search.maven.org/remotecontent?filepath=junit/junit/"$JUNIT_V"/junit-"$JUNIT_V".jar \
	--file "$SCRIPT_DIR"/junit-"$JUNIT_V".jar \
	--delete-pattern 'junit-*.jar'
H_V=1.3
download-kit \
	--url http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/"$H_V"/hamcrest-core-"$H_V".jar \
	--file "$SCRIPT_DIR"/hamcrest-core-"$H_V".jar \
	--delete-pattern 'hamcrest-core-*.jar'
