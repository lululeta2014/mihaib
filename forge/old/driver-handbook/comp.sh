#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

xelatex driver-handbook-notes.tex && xelatex driver-handbook-notes.tex
