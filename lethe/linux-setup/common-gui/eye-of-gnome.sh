#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


# Eye of Gnome – Image Viewer

# untick View → Side Pane
gsettings set org.gnome.eog.ui sidebar false
