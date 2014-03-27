#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


# Set the ‘last used tab’ to be ‘resources’ instead of the default ‘disks’,
# so even the first time we run the program we see the tab we want.
gsettings set org.gnome.gnome-system-monitor current-tab '"resources"'
