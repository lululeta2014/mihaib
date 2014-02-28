#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

# This script kills any existing ‘conky’ processes, then copies
# conkyrc.HOSTNAME.DESKTOP_SESSION to /tmp/conkyrc-USERNAME
# optionally changing own_window_type and own_window_hints
# (based on whether the top panel is set to autohide)
# after which it runs conky.
# This script can be run whenever the top panel's autohide option is changed.

killall -u `whoami` conky

if [ $? != "0" ]; then
	# no process killed, this is session startup not panel toggle
	# a small delay here works around several issues:
	# – starting in desktop (not panel) mode (if top panel is hidden)
	#   and getting hidden when XFCE draws the background picture
	# – having a window too short to include weather conditions

	sleep 3

	# if ‘sleep’ above gets commented, syntax requires a non-comment
	# between ´then’ and ‘fi’
	echo >/dev/null
fi

INFILE="$DIR/conkyrc.$(uname -n).$DESKTOP_SESSION"
OUTFILE="/tmp/conkyrc-$(whoami)"

if [ -e "$INFILE" ]; then
	rm -f "$OUTFILE"
	if [ "$DESKTOP_SESSION" == "xfce" ]; then
		HIDDEN=$(xfconf-query -c xfce4-panel -p '/panels/panel-0/autohide')
	else
		HIDDEN="false"
	fi
	if [ "$HIDDEN" == "true" ]; then
		sed -e 's/^own_window_type panel$/own_window_type desktop/'\
			-e 's/^own_window_hints above$/own_window_hints below/'\
			"$INFILE" >"$OUTFILE"
	else
		cp "$INFILE" "$OUTFILE"
	fi
	conky -c "$OUTFILE" &
fi
