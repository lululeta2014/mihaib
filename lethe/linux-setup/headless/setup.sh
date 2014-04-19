#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


cp -p /etc/skel/.bashrc ~

# using <<"EOF" instead of <<EOF means $VARIABLES are not expanded
cat >>~/.bashrc <<"EOF"

alias l='ls -lhF'
alias ll='ls -lhFA'
alias la='ls -A'

# alias e='nautilus .'
# alias e='thunar .'
# alias e='pcmanfm .'

alias e='if [ "$DESKTOP_SESSION" == gnome-fallback -o "$DESKTOP_SESSION" == gnome-classic ]; then nautilus .; elif [ "$DESKTOP_SESSION" == xfce ]; then thunar .; fi'

alias m3='mp3info -p "%r\n"'
alias m3a='mp3info -p "%r\n" -r a'
alias m3m='mp3info -p "%r\n" -r m'

# Using --bitrate makes jack think we're not in VBR mode and it checks the file
# size, sometimes deciding it's too small and removing it:
# ‘coding failed, err#242’
# Using --vbr=yes with --bitrate tells it we're in VBR mode but
# ‘helpers['oggenc']['vbr-cmd']’ passes -q not -b to oggenc.
# Fix by passing --quality instead of --bitrate to jack. Quality 8 ⇒ 256kbps.
alias rip-cd='jack --query-now --quality 8 --remove-files --dir-template "%a/%a - %y - %l" --rename-fmt "%n %t" --rename-fmt-va "%n %t"'

alias grep='grep --color=auto'


function trim-lines() {
	cut -c -`tput cols`
}

function s() {
if [ $# -lt 1 ]; then
	echo "Usage: s arg [arg2 ...] grep -i for 'arg arg2 ...'"
else
	grep -i -n -r --exclude-dir=.git -F -e "$*" . 2>/dev/null
fi
}

function s-() {
if [ $# -lt 1 ]; then
	echo "Usage: s- arg [arg2 ...] grep case sensitive for 'arg arg2 ...'"
else
	grep -n -r --exclude-dir=.git -F -e "$*" . 2>/dev/null
fi
}

function f() {
if [ $# -ne 1 ]; then
	echo 'Usage: f name --> find -iname '*'name'*' 2>/dev/null'
else
	find -iname '*'"$1"'*' 2>/dev/null
fi
}

function f-() {
if [ $# -ne 1 ]; then
	echo 'Usage: f- name --> find -name name 2>/dev/null'
else
	find -name "$1" 2>/dev/null
fi
}

function p() {
if [ $# -lt 1 ]; then
	echo "Usage: p arg [arg2 ...] --> ps aux | grep 'arg arg2 ...'"
else
	ps aux | grep "$*"
fi
}

function k() {
if [ $# -lt 1 ]; then
	echo "Usage: k pid [pid2 ...] --> kill pid [pid2 ...]"
else
	kill "$@"
fi
}

function k9() {
if [ $# -lt 1 ]; then
	echo "Usage: k9 pid [pid2 ...] --> kill -SIGKILL pid [pid2 ...]"
else
	kill -SIGKILL "$@"
fi
}
EOF

if [ $MB_PRIVATE_COMP == "1" ]
then
	echo >>~/.bashrc "
alias m='$MB_PRG_DIR/toolbox/money-trail/mt-start-stop.py --port 8765 `base64 -d <<<'L21udC9oYXZlbi9jcmFmdC9taXNjL21vbmV5LXRyYWlsLnNxbC5ncGcK'`'

alias sync-diff='sync-diff.py \\
	/mnt/haven/craft            /media/'"$MB_WHOAMI"'/Avatar/craft \\
	/mnt/haven/box              /media/'"$MB_WHOAMI"'/Avatar/box \\
	'
"
fi

if [ $MB_PRIVATE_COMP == 1 -a "$MB_MYSELF" == 'true' ]; then
	echo >>~/.bashrc \
"
alias ob='$MB_TOOLS_DEST/do-online-backup.py --force'
alias r-status='$MB_TOOLS_DEST/repos-status.py status'
alias r-pull='$MB_TOOLS_DEST/repos-status.py pull'
"
fi

# http://audio-and-linux.blogspot.fi/2011/07/mp3-player-with-jack-output.html
echo >>~/.bashrc "
alias u='sudo apt-get update && sudo apt-get dist-upgrade && sudo apt-get clean'
alias ur='sudo apt-get autoremove'
alias ap='alsaplayer -o jack -d system:playback_1,system:playback_2'
"


# Desktop
if [ "$MB_LINK_DESKTOP" == true ]; then
	if [ -d ~/Desktop -a ! -L ~/Desktop ]; then
		rmdir ~/Desktop
	fi
	if [ ! -e ~/Desktop -a ! -L ~/Desktop ]; then
		ln -s -f -T $MB_DESKTOP_DIR ~/Desktop
	fi
fi

if [ "$MB_MYSELF" == true ]; then
	if [ "$MB_PRIVATE_COMP" == 1 ]; then
		if [ ! -e ~/repos -a ! -L ~/repos ]; then
			ln -s -T "$MB_REPOS_DIR" ~/repos
		fi
	fi
	if [ ! -e ~/prg -a ! -L ~/prg ]; then
		ln -s -T "$MB_PRG_DIR" ~/prg
	fi
	if [ ! -e ~/byhand-prg -a ! -L ~/byhand-prg ]; then
		ln -s -T "$MB_BYHAND_PRG_DIR" ~/byhand-prg
	fi
fi


"$DIR"/vim.sh


# Java

mkdir -p ~/.mozilla/plugins
ln -f -s "$MB_PRG_DIR"/jdk/jre/lib/amd64/libnpjp2.so ~/.mozilla/plugins


"$DIR"/git.sh


# texttest

mkdir -p ~/.texttest
echo >~/.texttest/config \
'view_program:gedit
diff_program:meld'


# file manager bookmarks

if [ $MB_PRIVATE_COMP == "1" ]
then
	BOOKMARKS=\
"file:///mnt/haven
file:///mnt/haven/box/music
file:///mnt/eph/bt"
else
	BOOKMARKS=\
"file:///mnt/haven"
fi
echo >~/.gtk-bookmarks "$BOOKMARKS"
mkdir -p ~/.config/gtk-3.0
echo >~/.config/gtk-3.0/bookmarks "$BOOKMARKS"
unset BOOKMARKS


# .sudo_as_admin_successful
if [ ! -e ~/.sudo_as_admin_successful ]; then
	touch ~/.sudo_as_admin_successful
fi


"$DIR"/ssh.sh
"$DIR"/online-backup.sh
"$DIR"/jfs.sh
"$DIR/copy-tools.sh"
"$DIR"/ibus-keyboard.sh
