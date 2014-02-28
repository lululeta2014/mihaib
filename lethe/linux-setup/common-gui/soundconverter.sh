#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


gconftool-2 --type string --set /apps/SoundConverter/mp3-mode cbr
gconftool-2 --type string --set /apps/SoundConverter/output-mime-type audio/mpeg
gconftool-2 --type int --set /apps/SoundConverter/same-folder-as-input 0
gconftool-2 --type string --set /apps/SoundConverter/selected-folder \
	file://$HOME/Desktop
gconftool-2 --type int --set /apps/SoundConverter/create-subfolders 1
gconftool-2 --type int --set /apps/SoundConverter/subfolder-pattern-index 2
