#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

xelatex lang.impl.patterns
xelatex lang.impl.patterns
