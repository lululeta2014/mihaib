#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

java -jar "$DIR/java/dist/ft.jar" "$@"
