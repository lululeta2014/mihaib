#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

N=`xfconf-query -c xfce4-panel -p '/panels'`

if [ $N == "2" -o $N == "3" ]; then
	HIDDEN=$(xfconf-query -c xfce4-panel -p '/panels/panel-0/autohide')
	if [ $HIDDEN == "true" ]; then
		NEW_STATE="false"
	else
		NEW_STATE="true"
	fi

	xfconf-query -c xfce4-panel -p '/panels/panel-0/autohide' \
		-t bool -s $NEW_STATE -n
	if [ $N == "3" ]; then
		xfconf-query -c xfce4-panel -p '/panels/panel-1/autohide' \
			-t bool -s $NEW_STATE -n
	fi

	# conky.sh changes some properties if panel autohide is toggled
	"$DIR/conky.sh"
fi
