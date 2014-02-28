#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


for dir in Documents Downloads Music Pictures Public Templates Videos
do
	rm -rf ~/$dir
done
