#! /usr/bin/env bash

if [ $(dconf read /org/gnome/gnome-panel/layout/toplevels/bottom-panel/auto-hide) == true ]; then
	dconf write /org/gnome/gnome-panel/layout/toplevels/bottom-panel/auto-hide false
else
	dconf write /org/gnome/gnome-panel/layout/toplevels/bottom-panel/auto-hide true
fi
