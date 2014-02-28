#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


rm -rf ~/.config/geany
mkdir -p ~/.config/geany
echo > ~/.config/geany/geany.conf '
[geany]
sidebar_page=1
pref_main_load_session=false'
