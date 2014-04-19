#!/bin/bash

# Interrogate settings backends with:
# gsettings list-recursively
# gconftool-2 --recursive-list /
# dconf watch /

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


# nautilus
gsettings set org.gnome.desktop.background show-desktop-icons true
if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	gsettings set org.gnome.nautilus.desktop computer-icon-visible false
fi
gsettings set org.gnome.nautilus.desktop home-icon-visible false
gsettings set org.gnome.nautilus.desktop trash-icon-visible false
gsettings set org.gnome.nautilus.desktop volumes-visible true
gsettings set org.gnome.nautilus.preferences executable-text-activation "'display'"
if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	gsettings set org.gnome.nautilus.preferences show-hidden-files true
else
	dconf write /org/gtk/settings/file-chooser/show-hidden true
fi
gsettings set org.gnome.nautilus.preferences show-image-thumbnails "'never'"
if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	gsettings set org.gnome.nautilus.window-state start-with-status-bar true
fi

# Mouse & Touchpad tap to click
gsettings set org.gnome.settings-daemon.peripherals.touchpad tap-to-click true
gsettings set org.gnome.settings-daemon.peripherals.touchpad horiz-scroll-enabled true


# Theme settings
gsettings set org.gnome.desktop.interface menus-have-icons true
if [ "$MB_LSB_ID" == 'Debian' ]; then
	true
else
	# Font Hinting
	#gsettings set org.gnome.settings-daemon.plugins.xsettings hinting 'medium'
	gsettings set org.gnome.desktop.interface monospace-font-name "'Monospace 11'"
	#gsettings set org.gnome.desktop.interface cursor-theme "'Adwaita'"
	#gsettings set org.gnome.desktop.interface icon-theme "'gnome'"
	#gsettings set org.gnome.desktop.wm.preferences theme "'Adwaita'"
	gsettings set org.gnome.desktop.wm.preferences theme "'Mb-Metabox'"
	gsettings set org.gnome.desktop.interface gtk-theme "'Adwaita'"
fi

# DPI Setting (‘text scaling factor’ in gnome-tweak-tool)
if [ -v MB_DPI ]; then
	# If adjusting the value from gnome-tweak-tool, it's set to 1 decimal
	gsettings set org.gnome.desktop.interface text-scaling-factor `python3 <<<"print($MB_DPI/96)"`
fi

# window manager
#gconftool-2 --set /apps/metacity/general/action_middle_click_titlebar \
#		--type string toggle_maximize_vertically
#gconftool-2 --set /apps/metacity/general/action_right_click_titlebar \
#		--type string toggle_maximize_horizontally

if [ "$MB_LSB_ID" == 'Debian' ]; then
	true
else
	if [ "`gsettings get org.gnome.desktop.wm.preferences button-layout`" == "'close,minimize,maximize:'" ]; then
		gsettings set org.gnome.desktop.wm.preferences button-layout "':minimize,maximize,close'"
	fi
fi

# keyboard layouts
if [ -e "$MB_KB_LAYOUT_PATH" ]; then
	if [ "$MB_LSB_ID" == 'Debian' ]; then
		gsettings set org.gnome.libgnomekbd.keyboard layouts \
			"['mb', 'mb\tumlaut', 'us\taltgr-intl']"
	elif [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.10' ]; then
		gsettings set org.gnome.libgnomekbd.keyboard layouts \
			"['mb', 'mb\tumlaut', 'us\taltgr-intl']"
	else
		gsettings set org.gnome.desktop.input-sources sources \
			"[('xkb', 'mb'), ('xkb', 'mb+umlaut'), ('xkb', 'us+intl')]"
	fi
else
	echo "$MB_KB_LAYOUT_PATH" missing
	if [ "$MB_LSB_ID" == 'Debian' ]; then
		gsettings set org.gnome.libgnomekbd.keyboard layouts \
			"['ro', 'us\taltgr-intl']"
	elif [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.10' ]; then
		gsettings set org.gnome.libgnomekbd.keyboard layouts \
			"['ro', 'us\taltgr-intl']"
	else
		gsettings set org.gnome.desktop.input-sources sources \
			"[('xkb', 'ro'), ('xkb', 'us+intl')]"
	fi
fi
if [ "$MB_LSB_ID" == 'Debian' ]; then
	gsettings set org.gnome.libgnomekbd.keyboard options "['grp\tgrp:shifts_toggle']"
elif [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.10' ]; then
	gsettings set org.gnome.libgnomekbd.keyboard options "['grp\tgrp:shifts_toggle']"
else
	gsettings set org.gnome.desktop.wm.keybindings switch-input-source "['<Shift>Shift_R']"
	gsettings set org.gnome.desktop.wm.keybindings switch-input-source-backward "['<Shift><Super>Shift_R']"
fi

# screensaver
gsettings set org.gnome.desktop.screensaver lock-enabled true
gsettings set org.gnome.desktop.screensaver lock-delay 'uint32 0'
gsettings set org.gnome.desktop.session idle-delay \
	'uint32 '`python3 <<<'print('"$MB_SCREENSAVER_MINS"'*60)'`

if [ "$MB_LSB_ID" == 'Debian' ]; then
	if [ "$MB_LSB_REL" == 'wheezy' ]; then
		# after the time above, it takes 10 sec to fade the display,
		# plus 20 sec, plus the delay below until the display is turned off
		gsettings set org.gnome.settings-daemon.plugins.power sleep-display-ac 5
		gsettings set org.gnome.settings-daemon.plugins.power sleep-display-battery 5
	fi
else
	# Neither Ubuntu 13.10 nor Debian Jessie have these gsettings keys
	true
fi

# keyboard shortcuts

# Launchers
gsettings set org.gnome.settings-daemon.plugins.media-keys calculator "'<Super>q'"
gsettings set org.gnome.settings-daemon.plugins.media-keys www "'<Super>w'"
gsettings set org.gnome.settings-daemon.plugins.media-keys home "'<Super>e'"

custom_count=-1	# current custom shortcut nr

if [ "$MB_LSB_ID" == 'Debian' ]; then
	custom_count="$((custom_count+1))"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Terminal'"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-terminal'"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>t'"
else
	gsettings set org.gnome.settings-daemon.plugins.media-keys terminal "'<Super>t'"
fi

# Navigation
if false; then
	gsettings set org.gnome.desktop.wm.keybindings switch-group "['<Alt>grave']"
fi

gsettings set org.gnome.desktop.wm.keybindings show-desktop "['<Super>d']"

gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-1 "['<Primary>F1']"
gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-2 "['<Primary>F2']"
gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-3 "['<Primary>F3']"
gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-4 "['<Primary>F4']"

# Sound and Media
gsettings set org.gnome.settings-daemon.plugins.media-keys volume-down "'<Super>minus'"
gsettings set org.gnome.settings-daemon.plugins.media-keys volume-up "'<Super>equal'"
gsettings set org.gnome.settings-daemon.plugins.media-keys volume-mute "'<Super>0'"

# System

# For now this is broken in Debian Wheezy (it pops up the Shutdown Dialog)
# so adding it as a keyboard shortcut for Debian
if [ "$MB_LSB_ID" == 'Debian' ]; then
	custom_count="$((custom_count+1))"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Log Out'"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-session-quit --logout'"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>semicolon'"
else
	gsettings set org.gnome.settings-daemon.plugins.media-keys logout "'<Super>semicolon'"
fi

gsettings set org.gnome.settings-daemon.plugins.media-keys screensaver "'<Super>l'"

# Windows
gsettings set org.gnome.desktop.wm.keybindings toggle-fullscreen "['<Super>f']"

# this is set by default to ['<Super>Up', '<Alt>F10']
gsettings set org.gnome.desktop.wm.keybindings toggle-maximized "['<Super>Up']"

# this is set by default to ['<Super>Down', '<Alt>F5'], we need to reuse <Super>Down later
gsettings set org.gnome.desktop.wm.keybindings unmaximize "['']"
# In Ubuntu, this is a list including <Super>Up; we could also leave it alone
gsettings set org.gnome.desktop.wm.keybindings maximize "['']"

gsettings set org.gnome.desktop.wm.keybindings minimize "['<Super>Left']"

gsettings set org.gnome.desktop.wm.keybindings maximize-vertically "['<Super>Down']"
gsettings set org.gnome.desktop.wm.keybindings maximize-horizontally "['<Super>Right']"

# Custom Shortcuts
# 'gsettings get ...' or 'gsettings set ...' says 'No such schema'. Use 'dconf' instead.

if [ "$MB_LSB_ID" == Debian ]; then
	CHROMIUM_BIN='chromium'
else
	CHROMIUM_BIN='chromium-browser'
fi

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Terminal Default'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-terminal --window-with-profile=Default'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>1'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Terminal Black'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-terminal --window-with-profile=Black'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>2'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Terminal Green'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-terminal --window-with-profile=Green'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>3'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Terminal Yellow'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-terminal --window-with-profile=Yellow'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>4'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Drop-down terminal'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'xfce4-terminal --drop-down'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>t'"

# http://docs.xfce.org/apps/terminal/dropdown
custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Htop drop-down'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'xfce4-terminal --tab --drop-down --title \"htop\" -e htop'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Alt><Super>t'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Firefox $MB_BROWSER_ALT_PROFILE'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'firefox -P $MB_BROWSER_ALT_PROFILE -no-remote'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>w'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Chromium'"
if [ $MB_PRIVATE_COMP == 1 ]; then
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$CHROMIUM_BIN --password-store=gnome'"
else
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$CHROMIUM_BIN --password-store=gnome --incognito'"
fi
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>c'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Chromium $MB_BROWSER_ALT_PROFILE'"
if [ $MB_PRIVATE_COMP == 1 ]; then
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command \
		"'$CHROMIUM_BIN --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome'"
else
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command \
		"'$CHROMIUM_BIN --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome --incognito'"
fi
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>c'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Firefox Gmail (use first)'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command \
	"'$MB_TOOLS_DEST/browser-drive/firefox-gmail.py --use-first'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>m'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Firefox Gmail'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command \
	"'$MB_TOOLS_DEST/browser-drive/firefox-gmail.py'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>m'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'checkgmail'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command \
	"'$MB_TOOLS_DEST/checkgmail.py'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Alt><Super>m'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'kill checkgmail'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command \
	"'pkill -f checkgmail-gnome-keyring.pl'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Alt><Shift><Super>m'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Opera'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$MB_PRG_DIR/opera/opera -pd $MB_OPERA_REG_PROF_PATH'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>o'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Opera $MB_BROWSER_ALT_PROFILE'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$MB_PRG_DIR/opera/opera -pd $MB_OPERA_ALT_PROF_PATH'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>o'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Gedit'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gedit'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>g'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Mousepad'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'mousepad'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>g'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'toggle top gnome panel'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$MB_TOOLS_DEST/toggle-top-gnome3-panel.sh'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>k'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'toggle bottom gnome panel'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$MB_TOOLS_DEST/toggle-bottom-gnome3-panel.sh'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>j'"

# Currently, can't get custom keyboard shortcuts to work in Ubuntu 14.04.
# But when we do:
# In Ubuntu 14.04, Super+S opens the Applications menu. Disable it.
if [ "$MB_LSB_ID" == 'Debian' ]; then
	true
elif [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.10' ]; then
	true
else
	true
	# default is "['<Super>s', '<Alt>F1']"
	#gsettings set org.gnome.desktop.wm.keybindings \
		#panel-main-menu "['<Alt>F1']"
fi
custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Sound Preferencees'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-control-center sound'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>s'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Shut Down'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-session-quit --power-off'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>z'"

custom_count="$((custom_count+1))"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Display UTC Time'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'$MB_TOOLS_DEST/utc-time.sh'"
dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Shift><Super>d'"

CUSTOM_KEYBINDINGS=''
for (( i_custom_count=0; i_custom_count <= custom_count; i_custom_count++ ))
do
	NEW_CUST_KEYB="'"/org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$i_custom_count"/"'"
	if [ "$CUSTOM_KEYBINDINGS" == "" ]; then
		CUSTOM_KEYBINDINGS="$NEW_CUST_KEYB"
	else
		CUSTOM_KEYBINDINGS="$CUSTOM_KEYBINDINGS"", ""$NEW_CUST_KEYB"
	fi
done
gsettings set org.gnome.settings-daemon.plugins.media-keys custom-keybindings "[""$CUSTOM_KEYBINDINGS""]"

# media insertion: do nothing
gsettings set org.gnome.desktop.media-handling autorun-never true
