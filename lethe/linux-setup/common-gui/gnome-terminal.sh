#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR"/../sourceme.bash


if [ "$MB_LSB_ID" == 'Debian' ]; then
	if [ "$MB_LSB_CN" == 'wheezy' ]; then
		gconftool-2 --set \
			/apps/gnome-terminal/keybindings/toggle_menubar \
			--type string "F9"
	else
		gsettings set org.gnome.Terminal.Legacy.Settings \
			default-show-menubar false
		gsettings set org.gnome.Terminal.Legacy.Settings \
			mnemonics-enabled true
		# This has a relocatable schema and needs a PATH specified
		#gsettings set org.gnome.Terminal.Legacy.Keybindings \
		#	toggle-menubar "'F9'"
		# Try dconf instead:
		dconf write \
			/org/gnome/terminal/legacy/keybindings/toggle-menubar \
			"'F9'"
	fi
else
	gconftool-2 --set /apps/gnome-terminal/keybindings/toggle_menubar \
		--type string "F9"
fi

new_profiles() {
dconf reset -f /org/gnome/terminal/legacy/profiles:/
NEW_PROFILE=`uuidgen`
DEFAULT_PROFILE="$NEW_PROFILE"
PROF_LIST="'$NEW_PROFILE'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/visible-name" \
	"'Default'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/scrollbar-policy" \
	"'never'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/audible-bell" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/use-theme-colors" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/foreground-color" \
	"'rgb(0,0,0)'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/background-color" \
	"'rgb(255,255,255)'"

NEW_PROFILE=`uuidgen`
PROF_LIST="$PROF_LIST, '$NEW_PROFILE'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/visible-name" \
	"'Black'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/scrollbar-policy" \
	"'never'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/audible-bell" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/use-theme-colors" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/foreground-color" \
	"'rgb(255,255,255)'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/background-color" \
	"'rgb(0,0,0)'"

NEW_PROFILE=`uuidgen`
PROF_LIST="$PROF_LIST, '$NEW_PROFILE'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/visible-name" \
	"'Green'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/scrollbar-policy" \
	"'never'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/audible-bell" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/use-theme-colors" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/foreground-color" \
	"'rgb(0,255,0)'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/background-color" \
	"'rgb(0,0,0)'"

NEW_PROFILE=`uuidgen`
PROF_LIST="$PROF_LIST, '$NEW_PROFILE'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/visible-name" \
	"'Yellow'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/scrollbar-policy" \
	"'never'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/audible-bell" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/use-theme-colors" \
	false
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/foreground-color" \
	"'rgb(255,255,0)'"
dconf write "/org/gnome/terminal/legacy/profiles:/:$NEW_PROFILE/background-color" \
	"'rgb(0,0,0)'"

# List of profiles, and which is the default
dconf write /org/gnome/terminal/legacy/profiles:/list "[$PROF_LIST]"
dconf write /org/gnome/terminal/legacy/profiles:/default "'$DEFAULT_PROFILE'"
}

old_profiles() {
# 'Default' profile, on which the others are based
gconftool-2 --set /apps/gnome-terminal/profiles/Default/default_show_menubar \
	--type bool false

gconftool-2 --set /apps/gnome-terminal/profiles/Default/scrollback_lines \
	--type int 4096

gconftool-2 --set /apps/gnome-terminal/profiles/Default/silent_bell \
	--type bool true

gconftool-2 --set /apps/gnome-terminal/profiles/Default/scrollbar_position \
	--type string hidden

# 'Default' theme colors
gconftool-2 --set /apps/gnome-terminal/profiles/Default/use_theme_colors \
	--type bool false
gconftool-2 --set /apps/gnome-terminal/profiles/Default/background_color \
	--type string "#FFFFFFFFFFFF"
gconftool-2 --set /apps/gnome-terminal/profiles/Default/foreground_color \
	--type string '#000000000000'


# Create the new profiles as a copy of 'Default', then customize each
# --dump: outputs the gconf subtree to stdout
# --load FILE newtree: loads the subtree from FILE ('-' for stdin) into newtree

gconftool-2 --recursive-unset \
	/apps/gnome-terminal/profiles/Profile0 \
	/apps/gnome-terminal/profiles/Profile1 \
	/apps/gnome-terminal/profiles/Profile2

gconftool-2 --dump /apps/gnome-terminal/profiles/Default | gconftool-2 --load - /apps/gnome-terminal/profiles/Profile0
gconftool-2 --dump /apps/gnome-terminal/profiles/Default | gconftool-2 --load - /apps/gnome-terminal/profiles/Profile1
gconftool-2 --dump /apps/gnome-terminal/profiles/Default | gconftool-2 --load - /apps/gnome-terminal/profiles/Profile2

gconftool-2 --set /apps/gnome-terminal/profiles/Profile0/visible_name \
	--type string Black
gconftool-2 --set /apps/gnome-terminal/profiles/Profile0/background_color \
	--type string '#000000000000'
gconftool-2 --set /apps/gnome-terminal/profiles/Profile0/foreground_color \
	--type string '#FFFFFFFFFFFF'

gconftool-2 --set /apps/gnome-terminal/profiles/Profile1/visible_name \
	--type string Green
gconftool-2 --set /apps/gnome-terminal/profiles/Profile1/background_color \
	--type string '#000000000000'
gconftool-2 --set /apps/gnome-terminal/profiles/Profile1/foreground_color \
	--type string '#0000FFFF0000'

gconftool-2 --set /apps/gnome-terminal/profiles/Profile2/visible_name \
	--type string Yellow
gconftool-2 --set /apps/gnome-terminal/profiles/Profile2/background_color \
	--type string '#000000000000'
gconftool-2 --set /apps/gnome-terminal/profiles/Profile2/foreground_color \
	--type string '#FFFFFFFF0000'


# list of profiles, and which is default

gconftool-2 --set /apps/gnome-terminal/global/default_profile \
	--type string Default

gconftool-2 --set /apps/gnome-terminal/global/profile_list \
	--type list --list-type string '[Default,Profile0,Profile1,Profile2]'
}

if [ "$MB_LSB_ID" == 'Debian' ]; then
	if [ "$MB_LSB_CN" == 'wheezy' ]; then
		old_profiles
	else
		new_profiles
	fi
else
	old_profiles
fi
