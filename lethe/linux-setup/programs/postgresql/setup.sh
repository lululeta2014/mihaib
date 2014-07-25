#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../../sourceme.bash"


echo '--- PostgreSQL'

VERSION=9.3.5
LATEST=postgresql-"$VERSION".tar.bz2
PATTERN=postgresql-*.tar.bz2
download-kit \
	--delete-pattern "$PATTERN" \
	--url http://ftp.postgresql.org/pub/source/v"$VERSION"/"$LATEST" \
	--file "$MB_KITS_DIR"/"$LATEST"

PSQL_DIR="$MB_PRG_DIR"/postgresql
rm -rf "$PSQL_DIR"
tar -C "$MB_PRG_DIR" -xjf "$MB_KITS_DIR"/$PATTERN

cd "$MB_PRG_DIR"/postgresql-*
./configure --prefix="$PSQL_DIR"
make
make install
cd "$MB_PRG_DIR"
rm -rf postgresql-*

cp -r "$SCRIPT_DIR"/utils/* "$PSQL_DIR"/bin
postgresql-make-cluster.sh
