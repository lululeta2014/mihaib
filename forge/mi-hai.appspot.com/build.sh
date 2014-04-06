#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

PKG_ROOT='github.com/MihaiB/mihaib/forge/mi-hai.appspot.com/app'

golink --replace "$PKG_ROOT" "$DIR"/app

for X in \
	util \
	root \
;do
	#$APPENG_GO/goapp get -d "$PKG_ROOT"/"$X"
	$APPENG_GO/goapp fmt "$PKG_ROOT"/"$X"
	$APPENG_GO/goapp build "$PKG_ROOT"/"$X"
	$APPENG_GO/goapp test "$PKG_ROOT"/"$X"
done
