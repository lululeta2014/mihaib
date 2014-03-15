#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

rm -f \
	java-tutorial.aux \
	java-tutorial.log \
	java-tutorial.out
