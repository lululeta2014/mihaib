#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- Opera'

SRV_DIR=1216
LATEST=opera-12.16-1860.x86_64.linux.tar.xz
PATTERN='opera-*.x86_64.linux.tar.xz'
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url ftp://opera.ftp.fu-berlin.de/linux/"$SRV_DIR"/"$LATEST" \
	--delete-pattern "$PATTERN"

OPERA_DIR="$MB_PRG_DIR"/opera
rm -rf "$OPERA_DIR"
tar -C "$MB_PRG_DIR" -xJf "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/opera-*.x86_64.linux "$OPERA_DIR"

# prevent Opera from starting if run without the -pd flag
# because it won't be able to create a ‘profile’ dir
touch "$OPERA_DIR"/profile
touch "$OPERA_DIR"/README_use_pd_flag
