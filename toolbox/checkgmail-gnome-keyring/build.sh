#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`


# http://search.cpan.org/~mekk/
LATEST=Passwd-Keyring-Gnome-0.3002.tar.gz
PATTERN=Passwd-Keyring-Gnome-*.tar.gz
download-kit \
	--file "$SCRIPT_DIR"/"$LATEST" \
	--url http://search.cpan.org/CPAN/authors/id/M/ME/MEKK/"$LATEST" \
	--delete-pattern "$PATTERN" \
	|| \
	download-kit \
	--file "$SCRIPT_DIR"/"$LATEST" \
	--url http://cpan.metacpan.org/authors/id/M/ME/MEKK/"$LATEST" \
	--delete-pattern "$PATTERN"

PERL_LIBS_DIR="$SCRIPT_DIR"/perl-libs
rm -rf "$PERL_LIBS_DIR"
mkdir "$PERL_LIBS_DIR"


TMP_DIR=`mktemp -d`
(
cd "$TMP_DIR"
tar xzf "$SCRIPT_DIR"/$PATTERN
cd Passwd-Keyring-Gnome-*
perl Build.PL --install_base "$PERL_LIBS_DIR"
./Build
./Build install
)
rm -rf "$TMP_DIR"
