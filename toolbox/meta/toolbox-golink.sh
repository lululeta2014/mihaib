#! /usr/bin/env bash

set -u  # exit if using uninitialised variable
set -e  # exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
TOOLBOX_ROOT=`dirname "$SCRIPT_DIR"`

golink --replace github.com/MihaiB/toolbox "$TOOLBOX_ROOT"
