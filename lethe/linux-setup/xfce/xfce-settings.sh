#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


# Settings are in ~/.config/xfce4/xfconf/xfce-perchannel-xml/

xfconf-query -c xfce4-desktop -p '/desktop-icons/file-icons/show-home' -t bool -s false -n
xfconf-query -c xfce4-desktop -p '/desktop-icons/file-icons/show-filesystem' -t bool -s false -n
xfconf-query -c xfce4-desktop -p '/desktop-icons/file-icons/show-trash' -t bool -s false -n
xfconf-query -c xfce4-desktop -p '/desktop-icons/file-icons/show-removable' -t bool -s false -n
xfconf-query -c xfce4-desktop -p '/desktop-icons/show-thumbnails' -t bool -s false -n

# To reset Desktop settings
# xfconf-query -c xfce4-desktop -r -R -p '/desktop-icons/file-icons'


xfconf-query -c xfce4-keyboard-shortcuts -r -p '/commands/custom/<Control><Alt>Delete'
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>t' -t string -s 'exo-open --launch TerminalEmulator' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>1' -t string -s 'gnome-terminal --window-with-profile=Default' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>2' -t string -s 'gnome-terminal --window-with-profile=Black' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>3' -t string -s 'gnome-terminal --window-with-profile=Green' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>4' -t string -s 'gnome-terminal --window-with-profile=Yellow' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>t' -t string -s 'xfce4-terminal --drop-down' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>t' -t string -s 'xfce4-terminal --drop-down' -n
# http://docs.xfce.org/apps/terminal/dropdown
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Alt><Super>t' -t string \
	-s 'xfce4-terminal --tab --drop-down --title "htop" -e htop' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>e' -t string -s 'exo-open --launch FileManager' -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>q' -t string -s gnome-calculator -n
if [ "$MB_LSB_ID" == Debian ]; then
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>w' -t string -s iceweasel -n
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>w' -t string -n \
		-s "iceweasel -P $MB_BROWSER_ALT_PROFILE -no-remote"
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>w' -t string -n \
		-s "iceweasel -P $MB_BROWSER_ALT_PROFILE -no-remote"
else
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>w' -t string -s firefox -n
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>w' -t string -n \
		-s "firefox -P $MB_BROWSER_ALT_PROFILE -no-remote"
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>w' -t string -n \
		-s "firefox -P $MB_BROWSER_ALT_PROFILE -no-remote"
fi
# xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>w/startup-notify' -t bool -s true -n

if [ "$MB_LSB_ID" == Debian ]; then
	CHROMIUM_BIN='chromium'
else
	CHROMIUM_BIN='chromium-browser'
fi
if [ "$MB_PRIVATE_COMP" == "1" ]; then
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>c' -t string -s "$CHROMIUM_BIN"' --password-store=gnome' -n
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>c' -t string -n \
		-s "$CHROMIUM_BIN --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome"
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>c' -t string -n \
		-s "$CHROMIUM_BIN --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome"
else
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>c' -t string -s "$CHROMIUM_BIN"' --password-store=gnome --incognito' -n
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>c' -t string -n \
		-s "$CHROMIUM_BIN --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome --incognito"
	xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>c' -t string -n \
		-s "$CHROMIUM_BIN --user-data-dir=$HOME/.config/chromium-$MB_BROWSER_ALT_PROFILE --password-store=gnome --incognito"
fi
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>m' -t string -n \
	-s "$MB_TOOLS_DEST/browser-drive/firefox-gmail.py --use-first"
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>m' -t string -n \
	-s "$MB_TOOLS_DEST/browser-drive/firefox-gmail.py"
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>m' -t string -n \
	-s "$MB_TOOLS_DEST/browser-drive/firefox-gmail.py"

xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Alt><Super>m' -t string -n \
	-s "$MB_TOOLS_DEST/checkgmail.py"
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Alt><Super>m' -t string -n \
	-s "pkill --full checkgmail-gnome-keyring.pl"

xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>o' -t string -n \
	-s "$MB_PRG_DIR/opera/opera -pd $MB_OPERA_REG_PROF_PATH"
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>o' -t string -n \
	-s "$MB_PRG_DIR/opera/opera -pd $MB_OPERA_ALT_PROF_PATH"
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>o' -t string -n \
	-s "$MB_PRG_DIR/opera/opera -pd $MB_OPERA_ALT_PROF_PATH"
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>g' -t string -s gedit -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>g' -t string -s mousepad -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Control><Super>g' -t string -s mousepad -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>l' -t string -s xflock4 -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>k' -t string -s $MB_TOOLS_DEST/toggle-top-xfce-panel.sh -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>j' -t string -s $MB_TOOLS_DEST/toggle-bottom-xfce-panel.sh -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Alt>F1' -t string -s xfce4-popup-applicationsmenu -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/Print' -t string -s "scrot -e 'mv \$f ~/Desktop/'" -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Alt>Print' -t string -s "scrot -u -e 'mv \$f ~/Desktop/'" -n
# When launched from the keyboard shortcut, both scrot -s
# and gnome-screenshot -a complain. Use xfce4-screenshooter instead
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift>Print' -t string -s "xfce4-screenshooter -r -s $HOME/Desktop/" -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>z' -t string -s xfce4-session-logout -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>s' -t string -s "gnome-control-center sound" -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>minus' -t string -s "amixer set Master 5%-" -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>equal' -t string -s "amixer set Master 5%+" -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Super>0' -t string -s "amixer set Master toggle" -n
xfconf-query -c xfce4-keyboard-shortcuts -p '/commands/custom/<Shift><Super>d' -t string -s $MB_TOOLS_DEST/utc-time.sh -n


xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Super>Right' -n -t string -s maximize_horiz_key
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Super>Down' -n -t string -s maximize_vert_key
# Our maximize_window_key binding won't be picked up unless we delete the existing one
MAX_WINDOW_EXISTING=`xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Alt>F10'` || true
if [ "$MAX_WINDOW_EXISTING" == "maximize_window_key" ]; then
	xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Alt>F10' -r
fi
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Super>Up' -n -t string -s maximize_window_key
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Super>Left' -n -t string -s hide_window_key

xfconf-query -c xfce4-keyboard-shortcuts -r -p '/xfwm4/custom/<Alt>F11'
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Super>f' -n -t string -s fullscreen_key

xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Shift><Control><Alt>Up' -n -t string -s move_window_up_workspace_key
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Shift><Control><Alt>Down' -n -t string -s move_window_down_workspace_key
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Shift><Control><Alt>Left' -n -t string -s move_window_left_workspace_key
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Shift><Control><Alt>Right' -n -t string -s move_window_right_workspace_key

xfconf-query -c xfce4-keyboard-shortcuts -r -p '/xfwm4/custom/<Control><Alt>d'
xfconf-query -c xfce4-keyboard-shortcuts -p '/xfwm4/custom/<Super>d' -n -t string -s show_desktop_key


# Minimum size of windows to trigger smart placement
xfconf-query -c xfwm4 -p '/general/placement_ratio' -t int -s 0

# Don't raise background window on mouse scroll
xfconf-query -c xfwm4 -p '/general/raise_with_any_button' -t bool -s false

xfconf-query -c xfwm4 -p '/general/scroll_workspaces' -t bool -s false
xfconf-query -c xfwm4 -p '/general/snap_to_windows' -t bool -s true
# xfconf-query -c xfwm4 -p '/general/snap_width' -t int -s 5
# xfconf-query -c xfwm4 -p '/general/snap_resist' -t bool -s true
xfconf-query -c xfwm4 -p '/general/wrap_cycle' -t bool -s false
xfconf-query -c xfwm4 -p '/general/wrap_layout' -t bool -s false
xfconf-query -c xfwm4 -p '/general/wrap_windows' -t bool -s false
xfconf-query -c xfwm4 -p '/general/workspace_count' -t int -s 4

if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.04' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.10' -o \
	"$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-12.04' ]; then
	xfconf-query -c xfwm4 -p '/general/theme' -t string -s "Default-4.8"	# default "Default"
elif [ "$MB_LSB_ID" == 'Debian' ]; then
	xfconf-query -c xfwm4 -p '/general/theme' -t string -s "Default-4.8"	# default "Default"
else
	xfconf-query -c xfwm4 -p '/general/theme' -t string -s "Default"	# default "Default"
fi

if [ "$MB_LSB_ID" == Debian ]; then
	xfconf-query -c xfwm4 -p '/general/title_font' -t string -s "Sans Bold 11"	# default "Sans Bold 9"
else
	xfconf-query -c xfwm4 -p '/general/title_font' -t string -s "Sans Bold 10"	# default "Sans Bold 9"
	#xfconf-query -c xfwm4 -p '/general/title_font' -t string -s "Ubuntu Bold 11"	# default "Sans Bold 9"
fi


# Disabling ‘save session on logout’ will stop saving things, but maybe things
# which have already been saved in ~/.cache/sessions are still run at login.
xfconf-query -c xfce4-session -p '/general/SaveOnExit' -n -t bool -s false
rm -rf ~/.cache/sessions


# Todo: thunar options
# figure out if we want to change anything; properties are under
# xfconf-query -c thunar-volman -p '/automount-media/enabled' -t bool -s true
# delete settings with
# xfconf-query -c thunar-volman -r -R -p '/'

# preferred applications: file manager, terminal
echo 'FileManager=Thunar' >~/.config/xfce4/helpers.rc
# 'TerminalEmulator=' gnome-terminal or xfce4-terminal
TERM_EMULATOR=xfce4-terminal
echo "TerminalEmulator=$TERM_EMULATOR" >>~/.config/xfce4/helpers.rc

# Thunar settings
if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	# Show hidden files
	# You actually have to logout & log back in to see that this works
	THUNARRC=~/.config/Thunar/thunarrc
	if [ -e $THUNARRC ]
	then
		sed -i 's/^LastShowHidden=FALSE$/LastShowHidden=TRUE/' $THUNARRC
		sed -i 's/^MiscShowThumbnails=TRUE$/MiscShowThumbnails=FALSE/' $THUNARRC
	else
		echo '[Configuration]' >$THUNARRC
		echo 'LastShowHidden=TRUE' >>$THUNARRC
		echo 'MiscShowThumbnails=FALSE' >>$THUNARRC
		echo >>$THUNARRC
	fi
else
	xfconf-query -c thunar -p '/last-show-hidden' -t bool -s true -n
	xfconf-query -c thunar -p '/misc-thumbnail-mode' -t string -s THUNAR_THUMBNAIL_MODE_NEVER -n
fi


# Appearance
if [ "$MB_PRIVATE_COMP" == "1" ]; then
	xfconf-query -c xsettings -p '/Net/ThemeName' -t string -s Xfce
else
	xfconf-query -c xsettings -p '/Net/ThemeName' -t string -s "Xfce-orange"
fi

if [ "$MB_LSB_ID" == Debian ]; then
	xfconf-query -c xsettings -p '/Net/IconThemeName' -t string -s gnome	# default "Tango"
fi
if [ -v MB_DPI ]; then
	xfconf-query -n -c xsettings -p '/Xft/DPI' -t int -s "$MB_DPI"
fi
xfconf-query -c xsettings -p '/Xft/HintStyle' -t string -s hintslight	# default "hintnone"
# several font options
xfconf-query -c xsettings -p '/Gtk/FontName' -t string -s 'Sans 11'	# default "Sans 10"
if [ "$MB_LSB_ID" == Ubuntu ]; then
	true
	#xfconf-query -c xsettings -p '/Gtk/FontName' -t string -s 'Ubuntu 11'	# default "Sans 10"
fi


# Enable NumLock for future logins
# (avoid the need for a first manual toggle first time we use it)
# If we toggle the NumLock key, its value is saved on logout.
# Running this query by hand doesn't work (gets overwritten when we logout):
# xfconf-query -c keyboards -p '/Default/Numlock' -t bool -s true -n
# Running ‘numlockx’ will toggle the NumLock LED on the Keyboard and will work.
numlockx


# Removable drives management
xfconf-query -c thunar-volman -p '/automount-media/enabled' -t bool -s true -n
xfconf-query -c thunar-volman -p '/automount-drives/enabled' -t bool -s false -n
xfconf-query -c thunar-volman -p '/autobrowse/enabled' -t bool -s true -n


# Desktop background
if [ -v MB_XFCE_IMAGE_PATH ]; then
	xfconf-query -c xfce4-desktop -p /backdrop/screen0/monitor0/image-path -t string -s "$MB_XFCE_IMAGE_PATH" -n
fi


# Close laptop lid
xfconf-query -c xfce4-power-manager \
	-p /xfce4-power-manager/lid-action-on-ac -t uint -s 1 -n
xfconf-query -c xfce4-power-manager \
	-p /xfce4-power-manager/lid-action-on-battery -t uint -s 1 -n

#Power Management, Display
xfconf-query -c xfce4-power-manager \
	-p /xfce4-power-manager/dpms-on-battery-sleep \
	-t uint -s "$MB_SCREENSAVER_MINS" -n
xfconf-query -c xfce4-power-manager \
	-p /xfce4-power-manager/dpms-on-battery-off \
	-t uint -s `python3 <<<'print('"$MB_SCREENSAVER_MINS"'+1)'` -n
xfconf-query -c xfce4-power-manager \
	-p /xfce4-power-manager/dpms-on-ac-sleep \
	-t uint -s "$MB_SCREENSAVER_MINS" -n
xfconf-query -c xfce4-power-manager \
	-p /xfce4-power-manager/dpms-on-ac-off \
	-t uint -s `python3 <<<'print('"$MB_SCREENSAVER_MINS"'+1)'` -n


# Notification settings
xfconf-query -c xfce4-notifyd -p /expire-timeout -t int -s 5 -n
