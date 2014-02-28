#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
source "$DIR/../sourceme.bash"

"$DIR/checkgmail.sh"
"$DIR/browser-gmail.sh"
"$DIR/remove-premade-dirs.sh"
"$DIR/desktop-session-and-startup.sh"
"$DIR/totem.sh"
"$DIR/rhythmbox.py"
"$DIR/gedit.py"
"$DIR/geany.sh"
"$DIR/soundconverter.sh"
"$DIR/fonts.sh"
"$DIR/mime-types.sh"
"$DIR/gnome-terminal.sh"
"$DIR/pyroom-mihaib/pyroom-mihaib-setup.sh"
"$DIR/web-browsers.sh"
"$DIR/kid3.py"
"$DIR/vlc.py"
"$DIR/skype.sh"
"$DIR/xscreensaver.sh"
"$DIR/xfce4-terminal.sh"
"$DIR/gimp.py"
"$DIR/mousepad.sh"
"$DIR/mb-metabox/setup.sh"
"$DIR/eye-of-gnome.sh"
