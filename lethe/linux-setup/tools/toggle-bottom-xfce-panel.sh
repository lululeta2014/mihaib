#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

N=`xfconf-query -c xfce4-panel -p '/panels'`
P="$((N-1))"

if [ $(xfconf-query -c xfce4-panel -p "/panels/panel-$P/autohide") \
	== "true" ]; then
	NEW_STATE="false"
else
	NEW_STATE="true"
fi;

xfconf-query -c xfce4-panel -p "/panels/panel-$P/autohide" \
	-t bool -s $NEW_STATE -n
