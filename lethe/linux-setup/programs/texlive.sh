#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


echo '--- TexLive'
echo -n "Download if not present and install TexLive? [y/N] "
read OK
if [ "$OK" == "y" -o "$OK" == "Y" ]; then
	true
else
	echo "Canceled"
	exit
fi

# Use this for automatic mirror selection:
# http://mirror.ctan.org/systems/texlive/Images/
# or hard-code this http mirror:
# http://ftp.fau.de/ctan/systems/texlive/Images/

LATEST=texlive2013.iso
PATTERN='texlive2*.iso'
download-kit \
	--delete-pattern "$PATTERN" \
	--url http://mirror.ctan.org/systems/texlive/Images/"$LATEST" \
	--file "$MB_KITS_DIR"/"$LATEST"

TEXLIVE_DIR="$MB_PRG_DIR"/texlive
TEXLIVE_DIR_TMP="$MB_PRG_DIR"/texlive-tmp
rm -rf "$TEXLIVE_DIR_TMP"


# We're hard-coding the commands we're seding to the installer's text-ui below.
# But if the installer finds an existing TeXLive installation in your PATH,
# an extra first step appears: ‘Import settings from previous install y/n?’
# It seems to detect by doing ‘which tlmgr’. We'll remove our destination dir
# from the PATH, and if there's still a tlmgr reachable we'll error and exit.
PATH=`remove-path-entry "$TEXLIVE_DIR"/bin/x86_64-linux/`
if which tlmgr; then
	echo "ERROR: there's still a tlmgr in your PATH, see the comment in this script for an explanation"
	# ‘exit 1’ just exits, but ‘false’ triggers the ‘trap’ at the start
	# of this file which prints the script's name.
	false
fi


MOUNT_POINT=`mktemp -d`
echo Mounting "$MB_KITS_DIR"/"$LATEST" under "$MOUNT_POINT"
sudo mount -o loop "$MB_KITS_DIR"/"$LATEST" "$MOUNT_POINT"

COMMANDS_FILE=`mktemp`
# What we're configuring:
# Leaving detected binary platform (x86_64 GNU/Linux)
# Leaving default installation scheme: scheme-full
# Setting Directories: TEXDIR=/path/to/texlive-tmp
# Options:
#	Don't install font/macro doc tree
#	Don't install font/macro source tree
#	After installation, get package updates from CTAN → user choice
# Leave ‘create all format files’ checked, as it is by default.
# It looks like the *TeX system parses and loads a source file slowly,
# but can then produce a ‘format file’ so the next time it
# can quickly load the format file instead of the original source.
echo -n >"$COMMANDS_FILE" \
	'D
1
'"$TEXLIVE_DIR_TMP"'
R
O
D
S
'

echo -n "Update packages from the Internet after installation? [Y/n] "
read OK
if [ "$OK" == "" -o "$OK" == "y" -o "$OK" == "Y" ]; then
	true
else
	echo -n >>"$COMMANDS_FILE" 'Y
'
fi

echo -n >>"$COMMANDS_FILE" \
	'R
I
'

# -no-gui is the default anyway
# either invoke with ‘-portable’ or set the portable option from the (text) UI
cat "$COMMANDS_FILE" | \
	"$MOUNT_POINT"/install-tl -no-gui -portable

rm "$COMMANDS_FILE"
echo Unmounting "$MOUNT_POINT"
sudo umount "$MOUNT_POINT"
rmdir "$MOUNT_POINT"

rm -rf "$TEXLIVE_DIR"
mv "$TEXLIVE_DIR_TMP" "$TEXLIVE_DIR"
