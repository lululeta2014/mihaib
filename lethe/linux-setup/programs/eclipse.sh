#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- eclipse'

LATEST=eclipse-java-kepler-SR2-linux-gtk-x86_64.tar.gz
REL_DIR=kepler/SR2
PATTERN=eclipse-java-*-x86_64.tar.gz
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://ftp.ntua.gr/eclipse/technology/epp/downloads/release/"$REL_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

ECLIPSE_DIR="$MB_PRG_DIR"/eclipse
ECLIPSE_PREFS="$ECLIPSE_DIR"/configuration/.settings/org.eclipse.ui.ide.prefs
TMP_ECLIPSE_PREFS=/tmp/mihaib-"$MB_WHOAMI"-eclipse-prefs
ECLIPSE_PREFS_SAVED=false
if [ -e "$ECLIPSE_PREFS" ]; then
	cp "$ECLIPSE_PREFS" "$TMP_ECLIPSE_PREFS"
	ECLIPSE_PREFS_SAVED=true
fi
rm -rf "$ECLIPSE_DIR"
tar -C "$MB_PRG_DIR" -xzf "$MB_KITS_DIR"/$PATTERN
if [ "$ECLIPSE_PREFS_SAVED" == "true" ]; then
	ECLIPSE_PREFS_DIR=`dirname "$ECLIPSE_PREFS"`
	mkdir -p "$ECLIPSE_PREFS_DIR"
	mv "$TMP_ECLIPSE_PREFS" "$ECLIPSE_PREFS"
fi
