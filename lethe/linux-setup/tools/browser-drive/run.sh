#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

# you must be able to write
# chromedriver.log in the directory you're in when running this script,
# otherwise chromium will timeout without starting.
ant -q -f "$DIR"/build.xml

java -jar "$DIR"/dist/browser-drive.jar "$@"
rm -f chromedriver.log
