#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

# The .jar accepts no args; set the crt dir where the useful files are
cd "$DIR/dist"
java -jar "DiceLottery.jar"
