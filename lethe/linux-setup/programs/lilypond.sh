#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- LilyPond'

LATEST=lilypond-2.18.0-1.linux-64.sh
PATTERN=lilypond-*linux-64.sh
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://download.linuxaudio.org/lilypond/binaries/linux-64/"$LATEST" \
	--delete-pattern "$PATTERN"

LILYPOND_DIR="$MB_PRG_DIR"/lilypond
rm -rf "$LILYPOND_DIR"
mkdir "$LILYPOND_DIR"
chmod u+x "$MB_KITS_DIR"/$PATTERN
"$MB_KITS_DIR"/$PATTERN --prefix "$LILYPOND_DIR" --batch
