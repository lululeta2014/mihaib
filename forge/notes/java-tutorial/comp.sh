#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

xelatex java-tutorial.tex && xelatex java-tutorial.tex
