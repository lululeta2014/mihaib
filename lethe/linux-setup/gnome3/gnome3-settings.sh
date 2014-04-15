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
if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.10' -o \
	"$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	gsettings set org.gnome.nautilus.desktop computer-icon-visible false
fi
gsettings set org.gnome.nautilus.desktop home-icon-visible false
gsettings set org.gnome.nautilus.desktop trash-icon-visible false
gsettings set org.gnome.nautilus.desktop volumes-visible true
gsettings set org.gnome.nautilus.preferences executable-text-activation "'display'"
if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.04' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.10' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' -o \
	"$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	gsettings set org.gnome.nautilus.preferences show-hidden-files true
else
	dconf write /org/gtk/settings/file-chooser/show-hidden true
fi
gsettings set org.gnome.nautilus.preferences show-image-thumbnails "'never'"
if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.10' -o \
	"$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
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

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	if [ "`gconftool-2 --get /apps/metacity/general/button_layout`" == "close,minimize,maximize:" ]; then
		gconftool-2 --set /apps/metacity/general/button_layout -t string ':minimize,maximize,close'
	fi
elif [ "$MB_LSB_ID" == 'Debian' ]; then
	true
else
	if [ "`gsettings get org.gnome.desktop.wm.preferences button-layout`" == "'close,minimize,maximize:'" ]; then
		gsettings set org.gnome.desktop.wm.preferences button-layout "':minimize,maximize,close'"
	fi

	if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.10' ]; then
		# also change the old setting: it's used by the buttons in chromium-browser
		if [ "`gconftool-2 --get /apps/metacity/general/button_layout`" == "close,minimize,maximize:" ]; then
			gconftool-2 --set /apps/metacity/general/button_layout -t string ':minimize,maximize,close'
		fi
	fi
fi

# keyboard layouts
if [ -e "$MB_KB_LAYOUT_PATH" ]; then
	gsettings set org.gnome.libgnomekbd.keyboard layouts \
		"['mb', 'mb\tumlaut', 'us\taltgr-intl']"
else
	echo "$MB_KB_LAYOUT_PATH" missing
	gsettings set org.gnome.libgnomekbd.keyboard layouts \
		"['ro', 'us\taltgr-intl']"
fi
gsettings set org.gnome.libgnomekbd.keyboard options "['grp\tgrp:shifts_toggle']"

# screensaver
gsettings set org.gnome.desktop.screensaver lock-enabled true
gsettings set org.gnome.desktop.screensaver lock-delay 'uint32 0'
gsettings set org.gnome.desktop.session idle-delay \
	'uint32 '`python3 <<<'print('"$MB_SCREENSAVER_MINS"'*60)'`

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.04' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.10' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
# after the time above, it takes 10 sec to fade the display,
# plus 20 sec, plus the delay below until the display is turned off
gsettings set org.gnome.settings-daemon.plugins.power sleep-display-ac 5
gsettings set org.gnome.settings-daemon.plugins.power sleep-display-battery 5
elif [ "$MB_LSB_ID" == 'Debian' ]; then
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

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 \
		--set /apps/metacity/global_keybindings/run_command_terminal \
		--type string '<Super>t'
elif [ "$MB_LSB_ID" == 'Debian' ]; then
	custom_count="$((custom_count+1))"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/name "'Terminal'"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/command "'gnome-terminal'"
	dconf write /org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom"$custom_count"/binding "'<Super>t'"
else
	gsettings set org.gnome.settings-daemon.plugins.media-keys terminal "'<Super>t'"
fi

# Navigation
if false; then
	if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
		gconftool-2 --set /apps/metacity/global_keybindings/switch_group --type string '<Alt>grave'
	else
		gsettings set org.gnome.desktop.wm.keybindings switch-group "['<Alt>grave']"
	fi
fi

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 --set /apps/metacity/global_keybindings/show_desktop --type string '<Super>d'
else
	gsettings set org.gnome.desktop.wm.keybindings show-desktop "['<Super>d']"
fi

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 --set /apps/metacity/global_keybindings/switch_to_workspace_1 --type string '<Primary>F1'
	gconftool-2 --set /apps/metacity/global_keybindings/switch_to_workspace_2 --type string '<Primary>F2'
	gconftool-2 --set /apps/metacity/global_keybindings/switch_to_workspace_3 --type string '<Primary>F3'
	gconftool-2 --set /apps/metacity/global_keybindings/switch_to_workspace_4 --type string '<Primary>F4'
else
	gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-1 "['<Primary>F1']"
	gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-2 "['<Primary>F2']"
	gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-3 "['<Primary>F3']"
	gsettings set org.gnome.desktop.wm.keybindings switch-to-workspace-4 "['<Primary>F4']"
fi

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
if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 --set /apps/metacity/window_keybindings/toggle_fullscreen --type string '<Super>f'
else
	gsettings set org.gnome.desktop.wm.keybindings toggle-fullscreen "['<Super>f']"
fi

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 --set /apps/metacity/window_keybindings/toggle_maximized --type string '<Super>Up'
else
	# this is set by default to ['<Super>Up', '<Alt>F10']
	gsettings set org.gnome.desktop.wm.keybindings toggle-maximized "['<Super>Up']"
fi

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	true
else
	# this is set by default to ['<Super>Down', '<Alt>F5'], we need to reuse <Super>Down later
	gsettings set org.gnome.desktop.wm.keybindings unmaximize "['']"
	# In Ubuntu, this is a list including <Super>Up; we could also leave it alone
	gsettings set org.gnome.desktop.wm.keybindings maximize "['']"
fi

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 --set /apps/metacity/window_keybindings/minimize --type string '<Super>Left'
else
	gsettings set org.gnome.desktop.wm.keybindings minimize "['<Super>Left']"
fi

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	gconftool-2 --set /apps/metacity/window_keybindings/maximize_vertically --type string '<Super>Down'
	gconftool-2 --set /apps/metacity/window_keybindings/maximize_horizontally --type string '<Super>Right'
else
	gsettings set org.gnome.desktop.wm.keybindings maximize-vertically "['<Super>Down']"
	gsettings set org.gnome.desktop.wm.keybindings maximize-horizontally "['<Super>Right']"
fi

# Custom Shortcuts
if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>1'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'gnome-terminal --window-with-profile=Default'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Terminal Default'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>2'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'gnome-terminal --window-with-profile=Black'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Terminal Black'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>3'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'gnome-terminal --window-with-profile=Green'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Terminal Green'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>4'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'gnome-terminal --window-with-profile=Yellow'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Terminal Yellow'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Shift><Super>w'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string "firefox -P $MB_BROWSER_ALT_PROFILE -no-remote"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string "Firefox $MB_BROWSER_ALT_PROFILE"

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>c'
	if [ $MB_PRIVATE_COMP == 1 ]; then
		gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'chromium-browser --password-store=gnome'
	else
		gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'chromium-browser --password-store=gnome --incognito'
	fi
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Chromium'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Shift><Super>c'
	if [ $MB_PRIVATE_COMP == 1 ]; then
		gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string \
			"chromium-browser --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome"
	else
		gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string \
			"chromium-browser --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome --incognito"
	fi
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string "Chromium $MB_BROWSER_ALT_PROFILE"

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>m'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string \
		"$MB_TOOLS_DEST/browser-drive/firefox-gmail.py --use-first"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Firefox Gmail (use first)'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Shift><Super>m'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string \
		"$MB_TOOLS_DEST/browser-drive/firefox-gmail.py"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Firefox Gmail'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>o'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string "$MB_PRG_DIR/opera/opera -pd $MB_OPERA_REG_PROF_PATH"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Opera'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Shift><Super>o'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string "$MB_PRG_DIR/opera/opera -pd $MB_OPERA_ALT_PROF_PATH"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string "Opera $MB_BROWSER_ALT_PROFILE"

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>g'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'gedit'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Gedit'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>k'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string "$MB_TOOLS_DEST/toggle-top-gnome3-panel.sh"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'toggle top gnome panel'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>j'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string "$MB_TOOLS_DEST/toggle-bottom-gnome3-panel.sh"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'toggle bottom gnome panel'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>s'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string "gnome-control-center sound"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Sound Preferences'

	custom_count="$((custom_count+1))"
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/binding --type string '<Super>z'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/action --type string 'gnome-session-quit --power-off'
	gconftool-2 --set /desktop/gnome/keybindings/custom"$custom_count"/name --type string 'Shut Down'
else
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
fi

# media insertion: do nothing
gsettings set org.gnome.desktop.media-handling autorun-never true
