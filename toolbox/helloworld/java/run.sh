#! /usr/bin/env bash

set -u  # exit if using uninitialised variable

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

java -jar "$SCRIPT_DIR/dist/HelloWorld.jar" "$@"
