#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`


PERL_LIBS_DIR="$SCRIPT_DIR"/perl-libs
PERL5LIB="$PERL_LIBS_DIR"/lib/perl5 \
	"$SCRIPT_DIR"/checkgmail-gnome-keyring.pl "$@"
