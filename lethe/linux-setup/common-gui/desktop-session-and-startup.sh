#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


mkdir -p ~/.config/autostart

# Disable some known startup items
# might consider adding update-notifier.desktop
for item in blueman.desktop bluetooth-applet.desktop deja-dup-monitor.desktop \
evolution-alarm-notify.desktop notification-daemon.desktop \
telepathy-indicator.desktop \
tracker-miner-fs.desktop tracker-store.desktop \
ubuntuone-launch.desktop update-notifier.desktop zeitgeist-datahub.desktop
do
	if [ -f /etc/xdg/autostart/$item ]
	then
		# overwrite existing, just in case
		cp /etc/xdg/autostart/$item ~/.config/autostart/
		# XFCE wants this
		echo 'Hidden=true' >>~/.config/autostart/$item
		# Gnome3 wants this
		echo 'X-GNOME-Autostart-enabled=false' >>~/.config/autostart/$item
	else
		echo $item not found in /etc/xdg/autostart/
	fi
done

ETC_SOUND_APPLET=/etc/xdg/autostart/gnome-sound-applet.desktop
if [ -f "$ETC_SOUND_APPLET" ]; then
	cp "$ETC_SOUND_APPLET" ~/.config/autostart/
	# offending properties: OnlyShowIn, maybe AutostartCondition
	# it seems that commenting them out loads them from /etc/xdg/autostart/
	# so we need to replace their value instead of commenting the line
	sed -i ~/.config/autostart/gnome-sound-applet.desktop \
		-e 's/^\(OnlyShowIn=\).*$/\1GNOME;XFCE;/' \
		-e 's/^\(AutostartCondition=\).*$/#\1/'
fi

ETC_NOTIFY_OSD=/etc/xdg/autostart/notify-osd.desktop
if [ -f "$ETC_NOTIFY_OSD" ]; then
	cp "$ETC_NOTIFY_OSD" ~/.config/autostart/
	sed -i ~/.config/autostart/notify-osd.desktop \
		-e 's/^\(OnlyShowIn=\).*$/\1GNOME;/' \
		-e '/^X-GNOME-Autostart-enabled=/d'
else
	# this the file in /etc/xdg/autostart on Debian Wheezy
	echo >~/.config/autostart/notify-osd.desktop \
'[Desktop Entry]
Name=Notify OSD
Comment=Display notifications
Exec=/usr/lib/notify-osd/notify-osd
Terminal=false
Type=Application
OnlyShowIn=GNOME;
'
fi

echo >~/.config/autostart/xfce4-notifyd.desktop \
'[Desktop Entry]
Version=1.0
Type=Application
Terminal=false
Name=xfce4-notifyd
Exec=/usr/lib/xfce4/notifyd/xfce4-notifyd
StartupNotify=false
OnlyShowIn=XFCE;
'

# Add our own scripts

echo -n >~/.config/autostart/checkgmail.desktop \
"[Desktop Entry]
Version=1.0
Type=Application
Terminal=false
Name=checkgmail
Exec=$MB_TOOLS_DEST/checkgmail.py
StartupNotify=false
Hidden=false
"

echo >~/.config/autostart/conky.desktop \
"[Desktop Entry]
Version=1.0
Type=Application
Terminal=false
Name=conky
Exec=$MB_TOOLS_DEST/conky.sh
StartupNotify=false
Hidden=false
"

# Extend this script in your fork below this line
