#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/sourceme.bash"


# Adds PATH entries for everything. Run before the other scripts.
cp -p /etc/skel/.bash_logout /etc/skel/.profile ~

# avoid double-adding entries to the path,
# such as /sbin which is missing on Debian but present on Ubuntu.
cat >>~/.profile <<"EOF"
add_to_path_start() {
if [ $# -ne 1 ]; then
	echo 'Usage: add_to_path_start entry'
else
	if ! echo "$PATH" | grep -q '\(^\|:\)'"$1"'\(:\|$\)'; then
		PATH="$1":"$PATH"
	fi
fi
}

add_to_path_end() {
if [ $# -ne 1 ]; then
	echo 'Usage: add_to_path_end entry'
else
	if ! echo "$PATH" | grep -q '\(^\|:\)'"$1"'\(:\|$\)'; then
		PATH="$PATH":"$1"
	fi
fi
}
EOF

echo >>~/.profile "
add_to_path_end /sbin	# useful on Debian, e.g. /sbin/ifconfig
add_to_path_end $MB_PRG_DIR/chromedriver
add_to_path_end $MB_PRG_DIR/texttest/source/bin
add_to_path_start $MB_PRG_DIR/rabbitmq/sbin
# postgresql must be at start of path because some programs (e.g. psycopg2)
# use pg_config to find out your installed version and you might already have
# a system-wide one available.
add_to_path_start $MB_PRG_DIR/postgresql/bin
add_to_path_start $MB_PRG_DIR/vagrant/bin
add_to_path_start $MB_PRG_DIR/node/bin
add_to_path_start $MB_PRG_DIR/adobe-reader/Adobe/Reader9/bin
add_to_path_start $MB_PRG_DIR/pyroom
add_to_path_start $MB_PRG_DIR/texlive/bin/x86_64-linux
add_to_path_start $MB_PRG_DIR/lilypond/bin
add_to_path_start $MB_PRG_DIR/toolbox/bin

APPENG_JAVA=$MB_PRG_DIR/appengine-java-sdk
APPENG_GO=$MB_PRG_DIR/go_appengine
export APPENG_JAVA APPENG_GO

GOROOT=$MB_GOROOT
GOPATH=$MB_GOPATH_DIR
export GOROOT GOPATH

add_to_path_start $MB_GOROOT/bin
add_to_path_start $MB_PRG_DIR/ant/bin
add_to_path_start $MB_PRG_DIR/jdk/bin


EDITOR=vim
export EDITOR
"

if [ -v MB_VAGRANT_HOME ]; then
	echo >>~/.profile "
VAGRANT_HOME=$MB_VAGRANT_HOME
export VAGRANT_HOME
"
fi


# Extend this script in your fork below this line
