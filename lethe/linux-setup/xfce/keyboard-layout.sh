#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR


# Uncheck ‘use system defaults’
xfconf-query -c keyboard-layout -p '/Default/XkbDisable' -t bool -s false -n
xfconf-query -c keyboard-layout -p '/Default/XkbOptions/Group' \
	-t string -s 'grp:shifts_toggle' -n
xfconf-query -c keyboard-layout -p '/Default/XkbLayout' -t string \
	-s 'mb,mb,us' -n
xfconf-query -c keyboard-layout -p '/Default/XkbVariant' -t string \
	-s ',umlaut,' -n
