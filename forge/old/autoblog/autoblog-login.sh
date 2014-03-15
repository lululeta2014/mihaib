#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

let "x = 30 + $RANDOM % 300"
sleep $x
"$DIR/autoblog.sh" >>"$DIR/out" 2>>"$DIR/err"
