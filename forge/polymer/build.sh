#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

cd "$DIR"

bower install polymer Polymer/polymer-elements Polymer/polymer-ui-elements
#bower update

npm install selenium-webdriver


# download QUnit
QUNIT_VER=1.14.0
QUNIT_TARGET_DIR="$DIR/qunit"
mkdir -p "$QUNIT_TARGET_DIR"
download-kit \
	-u http://code.jquery.com/qunit/qunit-"$QUNIT_VER".js \
	-f "$QUNIT_TARGET_DIR"/qunit.js
download-kit \
	-u http://code.jquery.com/qunit/qunit-"$QUNIT_VER".css \
	-f "$QUNIT_TARGET_DIR"/qunit.css

download-kit \
	-u https://raw.githubusercontent.com/jquery/qunit-composite/master/qunit-composite.js \
	-f "$QUNIT_TARGET_DIR"/qunit-composite.js
download-kit \
	-u https://raw.githubusercontent.com/jquery/qunit-composite/master/qunit-composite.css \
	-f "$QUNIT_TARGET_DIR"/qunit-composite.css
