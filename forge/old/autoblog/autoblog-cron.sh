#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

let "x = 60 + $RANDOM % 3600"
sleep $x
"$DIR/autoblog.sh" >>"$DIR/out" 2>>"$DIR/err"
