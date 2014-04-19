#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"

if [ "$MB_MYSELF" == 'true' ]; then
	mkdir -p ~/.ssh
	# don't fail if there are no keys
	if [ -v MB_SSH_DIR ]; then
		cp "$MB_SSH_DIR"/* ~/.ssh || true
	fi
	chmod go-rwx ~/.ssh/*id_rsa || true

	# Generated (so we can run this script offline) using:
	# ssh-keyscan -H domain.com IP.ADDR domain2.com IP2.ADDR
	# Couldn't automate: ssh-keyscan -H domain.com `dig +short domain.com`
	# because sometimes 'dig +short' prints multiple lines, including
	# alias names (so not only IPs) (e.g. dig +short www.google.com).
	# Didn't really want that, so checked the IP and ran the script by hand.

	cp "$DIR"/known_hosts ~/.ssh/
fi

# SSH Keep Alive
mkdir -p ~/.ssh
# Idempotency, don't append another copy of ServerAliveInterval on every run
if ! grep -s -q ServerAliveInterval ~/.ssh/config; then
	echo >>~/.ssh/config 'Host *
    ServerAliveInterval 60'
fi
