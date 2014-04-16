#! /usr/bin/env bash

# The crontab entry looks like:
# */30 * * * *	bash --login ~/.mb-tools/wallpaper/cron.sh /path/to/images 1 6 >/tmp/out-wallpaper 2>/tmp/err-wallpaper

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

if [ $# -ne 3 ]; then
	echo "$SCRIPT" needs 3 arguments "pics_dir K N"
	exit 1
fi

BG_DIR=$1
K=$2
N=$3

source "$DIR"/../gnomekeyring.bash

commands=`gkr-get-vars`
if [ $? == 0 ]; then
	eval $commands
	if chance-k-n $K $N; then
		"$DIR/random-wallpaper.py" "$BG_DIR" -- \
			--resize scaled --black-bg
	else
		echo "‘chance-k-n $K $N’ returned false"
	fi
else
	echo `date` "Could not find user session for gnome keyring variables"
fi
