#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


gsettings set org.gnome.totem show-visualizations false
# key missing in Debian Jessie
#gsettings set org.gnome.totem lock-screensaver-on-audio false
