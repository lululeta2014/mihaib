#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- Adobe Reader (makes changes in your home directory)'

VERSION=9.5.5
SRV_DIR=9.x/"$VERSION"
LATEST=AdbeRdr"$VERSION"-1_i486linux_enu.tar.bz2
PATTERN=AdbeRdr[0-9]*_i486linux_enu.tar.bz2
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://ardownload.adobe.com/pub/adobe/reader/unix/"$SRV_DIR"/enu/"$LATEST" \
	--delete-pattern "$PATTERN"

AR_DIR="$MB_PRG_DIR"/adobe-reader
rm -rf "$AR_DIR"
# Create the dir, so the installer doesn't ask for confirmation to create it.
mkdir "$AR_DIR"

AR_KIT_DIR="$MB_PRG_DIR"/kit-adobe-reader
rm -rf "$AR_KIT_DIR"
mkdir "$AR_KIT_DIR"
tar -C "$AR_KIT_DIR" -xjf "$MB_KITS_DIR"/$PATTERN
"$AR_KIT_DIR"/AdobeReader/INSTALL --install_path="$AR_DIR"
rm -rf "$AR_KIT_DIR"

# The installation puts a lot of files/dirs in your home dir.
# Menu icons, a browser plugin, etc.
rm -f ~/Desktop/AdobeReader.desktop
rm -f ~/AdobeReader.desktop

# Don't want the uninstall script in the PATH
rm "$AR_DIR"/Adobe/Reader9/bin/UNINSTALL
