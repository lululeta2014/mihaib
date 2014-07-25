#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- rabbitmq'

VERSION=3.3.4
LATEST=rabbitmq-server-generic-unix-"$VERSION".tar.gz
PATTERN=rabbitmq-server-generic-unix-*.tar.gz
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url http://www.rabbitmq.com/releases/rabbitmq-server/v"$VERSION"/"$LATEST" \
	--delete-pattern "$PATTERN"

RMQ_DIR="$MB_PRG_DIR"/rabbitmq
rm -rf "$RMQ_DIR"
tar -C "$MB_PRG_DIR" -xzf "$MB_KITS_DIR"/$PATTERN
mv "$MB_PRG_DIR"/rabbitmq_server-* "$RMQ_DIR"
