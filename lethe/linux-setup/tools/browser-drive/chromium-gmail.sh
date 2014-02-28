#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

# clean up after previous instances.
# Unlike with Firefox, when using Chromium things don't finish nicely when
# the user closes the browser if we just drop off the end of the Java program
# without calling driver.quit().
killall -SIGKILL chromedriver

"$DIR"/run.sh chromium-gmail "$@"
