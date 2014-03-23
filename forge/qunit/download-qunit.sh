#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

TARGET_DIR="$DIR/lib"
mkdir -p "$TARGET_DIR"

VER=1.14.0
download-kit \
	-u http://code.jquery.com/qunit/qunit-"$VER".js \
	-f "$TARGET_DIR"/qunit.js
download-kit \
	-u http://code.jquery.com/qunit/qunit-"$VER".css \
	-f "$TARGET_DIR"/qunit.css

download-kit \
	-u https://raw.githubusercontent.com/jquery/qunit-composite/master/qunit-composite.js \
	-f "$TARGET_DIR"/qunit-composite.js
download-kit \
	-u https://raw.githubusercontent.com/jquery/qunit-composite/master/qunit-composite.css \
	-f "$TARGET_DIR"/qunit-composite.css
