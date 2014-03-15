#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
cd "$DIR"

rm -f \
	driver-handbook-notes.aux \
	driver-handbook-notes.log \
	driver-handbook-notes.out \
	driver-handbook-notes.toc
