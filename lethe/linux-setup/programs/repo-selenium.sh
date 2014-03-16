#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- selenium for this repository'
# http://seleniumhq.org/download/

VERSION=2.40
LATEST=selenium-java-"$VERSION".0.zip
PATTERN=selenium-java-*.zip
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://selenium-release.storage.googleapis.com/"$VERSION"/"$LATEST" \
	--delete-pattern "$PATTERN"

(
cd "$SCRIPT_DIR"/../tools/
git clean -fdx browser-drive
)
unzip -q -d "$SCRIPT_DIR"/../tools/browser-drive/lib \
	"$MB_KITS_DIR"/$PATTERN '*.jar'
ant -q -f "$SCRIPT_DIR"/../tools/browser-drive/build.xml rebuild
