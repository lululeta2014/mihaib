#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


# Disable iBus Keyboard Input.
# This belongs in ‘common-gui’, but for now doing it
# before the first graphical login.

# Found this by going to gnome-control-center → Language Support
# and setting Keyboard input method system to none.

echo 'run_im xim' >~/.xinputrc
