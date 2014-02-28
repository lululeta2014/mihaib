#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


# xscreensaver (sort of hack)
# blank screen, require password True/False,
# fade (on by default) for 1 second (default 3 seconds)
# set all dpms options; dpmsQuickOff just by itself doesn't seem to work

echo >~/.xscreensaver \
'timeout:	0:'`python3 <<<'print("{:02d}".format('"$MB_SCREENSAVER_MINS"'))'`':00
lock:		True
mode:		blank
#fadeSeconds:	0:00:01

dpmsEnabled:	True
dpmsStandby:	0:'`python3 <<<'print("{:02d}".format('"$MB_SCREENSAVER_MINS"'))'`':00
dpmsSuspend:	0:'`python3 <<<'print("{:02d}".format('"$MB_SCREENSAVER_MINS"'))'`':00
dpmsOff:	0:'`python3 <<<'print("{:02d}".format('"$MB_SCREENSAVER_MINS"'))'`':00
dpmsQuickOff:	True'
