#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

rm \
lang.impl.patterns.aux \
lang.impl.patterns.log \
lang.impl.patterns.out
