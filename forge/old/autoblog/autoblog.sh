#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source ~/.gnomekeyring-bash

commands=`gkr-get-vars`
if [ $? == 0 ]; then
	eval $commands
	"$DIR/main.py"
else
	echo `date` "Could not find user session for gnome keyring variables"
fi
