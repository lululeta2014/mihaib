#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


mkdir -p ~/.config/Mousepad
echo -n >~/.config/Mousepad/mousepadrc \
	'[Configuration]
ViewColorScheme=kate
ViewLineNumbers=true
ViewAutoIndent=true
ViewWordWrap=true
'
