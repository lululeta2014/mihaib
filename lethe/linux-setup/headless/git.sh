#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


git config --global color.ui auto
git config --global alias.s status
git config --global alias.d diff
git config --global alias.dc 'diff --cached'
git config --global alias.b branch

mkdir -p ~/.config/git
echo >~/.config/git/ignore '# vim swap files
.*.swp
'

if [ "$MB_WHOAMI"'@'"$MB_HOSTNAME" == 'mihai@Castor' ]; then
	git config --global user.name mihaib
	git config --global user.email 'mihaib@castor'
elif [ "$MB_WHOAMI"'@'"$MB_HOSTNAME" == 'mihai@Hermes' ]; then
	git config --global user.name mihaib
	git config --global user.email 'mihaib@hermes'
elif [ "$MB_PRIVATE_COMP" == "1" -a "$MB_MYSELF" == "true" ]; then
	echo "Don't know git name and email for $MB_WHOAMI@$MB_HOSTNAME"
fi
