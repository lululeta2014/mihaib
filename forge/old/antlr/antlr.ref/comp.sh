#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

xelatex antlr.ref
xelatex antlr.ref
