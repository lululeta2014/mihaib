#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- vagrant'

LATEST=vagrant_1.6.3_x86_64.deb
PATTERN=vagrant_[0-9]*_x86_64.deb
download-kit \
	--file "$MB_KITS_DIR"/"$LATEST" \
	--url https://dl.bintray.com/mitchellh/vagrant/"$LATEST" \
	--delete-pattern "$PATTERN"

VAGRANT_DIR="$MB_PRG_DIR"/vagrant
rm -rf "$VAGRANT_DIR"
mkdir "$VAGRANT_DIR"
cd "$VAGRANT_DIR"
ar p "$MB_KITS_DIR"/$PATTERN data.tar.gz | tar xz

# Now we have ./usr/bin/vagrant which simply forwards all its arguments to
# absolute path /opt/vagrant/bin/vagrant which resolves its own path and is
# not hardcoded to live under /opt.
# Delete ./usr.
# ./opt/vagrant/bin/ has a single file. We could put it in PATH or, to avoid
# future versions putting more files in that dir, and thus in our PATH,
# we'll make ./bin/ with one symlink to ./opt/vagrant/bin/vagrant and put ./bin
# in our PATH. Both ways probably work fine.
mkdir bin
ln -s ../opt/vagrant/bin/vagrant bin/vagrant
