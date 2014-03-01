#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`

source "$SCRIPT_DIR/../sourceme.bash"


if [ "$MB_PRIVATE_COMP" == "1" ]; then
	echo >~/.mb-jfs-profile.xml \
'<?xml version="1.0" encoding="UTF-8"?>
<jFileSync setcanwrite="false" title="Sync craft and box" version="2.2">
  <directory src="/mnt/haven/craft" tgt="/media/'"$MB_WHOAMI"'/Avatar/craft"/>
  <directory src="/mnt/haven/box" tgt="/media/'"$MB_WHOAMI"'/Avatar/box"/>
</jFileSync>'

	echo >>~/.bashrc "
alias jfs='java -jar $MB_PRG_DIR/jfilesync/lib/jfs.jar \\
	-laf javax.swing.plaf.nimbus.NimbusLookAndFeel \\
	-config ~/.mb-jfs-profile.xml'
"

	if [ -d ~/.jfs -a ! -L ~/.jfs ]; then
		rmdir ~/.jfs
	fi
	ln -s -f -T "$MB_BYHAND_PRG_DIR/jfs-$MB_WHOAMI" ~/.jfs
fi
