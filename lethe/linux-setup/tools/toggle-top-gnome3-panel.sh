#! /usr/bin/env bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

if [ $(dconf read /org/gnome/gnome-panel/layout/toplevels/top-panel/auto-hide) == true ]; then
	dconf write /org/gnome/gnome-panel/layout/toplevels/top-panel/auto-hide false
else
	dconf write /org/gnome/gnome-panel/layout/toplevels/top-panel/auto-hide true
fi

# conky.sh changes some properties if panel autohide is toggled
"$DIR/conky.sh"
