#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

# If we increment variables with ((i++)) or ((++i)), we will go through 0
# (we have vars starting at 0 and at -1) which upsets 'set -e' because of the
# return value ((i++)) produces (0 if the expresion i++ evaluates to non-zero,
# 1 otherwise). This also breaks «let "i=i+1"».
# We'll use «i="$((i+1))"» to increment.

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`
LAUNCHERS_DIR=`dirname "$DIR"`/launchers

source "$DIR/../sourceme.bash"


# setup_launcher number($1) launcher.desktop($2)
function setup_launcher() {
xfconf-query -c xfce4-panel -p "/plugins/plugin-$1" -t string -s 'launcher' -n
mkdir ~/.config/xfce4/panel/launcher-$1/
sed "$LAUNCHERS_DIR"/$2 \
	-e "s|\$MB_PRG_DIR|$MB_PRG_DIR|g" \
	-e "s|\$MB_BYHAND_PRG_DIR|$MB_BYHAND_PRG_DIR|g" \
	-e "s|\$MB_OPERA_REG_PROF_PATH|$MB_OPERA_REG_PROF_PATH|g" \
	> ~/.config/xfce4/panel/launcher-$1/$2
xfconf-query -c xfce4-panel -p "/plugins/plugin-$1/items" -t string -s $2 -a -n
}


# IMPORTANT
# this scrips kills xfce4-panel and restarts it when it's finished
killall xfce4-panel

# reset panels (part 1: delete existing configuration)
xfconf-query -c xfce4-panel -p '/panels' -r -R

# can't find the definition of the enumeration used for p=,
# but the default config uses 6 (top left, horiz) and 10 (bot center, horiz)
#
# After experimenting:
# 2 – top right, horiz; 11 – same as 6; 9 – top center, horiz


# plugins
xfconf-query -c xfce4-panel -p '/plugins' -r -R
rm -rf ~/.config/xfce4/panel
mkdir ~/.config/xfce4/panel


# top panel plugins
i=0	# current plugin nr
j=1	# builds args for 'plugin-ids' for the panel
p=-1	# crt panel no

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'applicationsmenu' -n

# we have 2 options for adding custom launchers
# 1)
# -p '/plugins/plugins-2/items' -t string -s '/path/to/custom.desktop' -a -n
# this creates ~/.config/xfce4/panel/launcher-2/«ID».desktop which copies the
# fields and also mentions X-XFCE-Source=/path/to/custom.desktop
# 2)
# mkdir ~/.config/xfce4/panel/launcher-2/
# ln -s /path/to/custom.desktop ~/.config/xfce4/panel/launcher-2/custom.desktop
# -p '/plugins/plugin-2/items' -t string -s 'custom.desktop' -a -n

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'launcher' -n
FIREFOX_LAUNCHER=firefox.desktop
if [ "$MB_LSB_ID" == Debian ]; then
	FIREFOX_LAUNCHER=iceweasel.desktop
fi
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/items" -t string -s "$LAUNCHERS_DIR/$FIREFOX_LAUNCHER" -a -n

i="$((i+1))"
if [ $MB_PRIVATE_COMP == "1" ]; then
	if [ "$MB_LSB_ID" == Debian ]; then
		CHROMIUM_DESKTOP=chromium.debian.desktop
	else
		CHROMIUM_DESKTOP=chromium.desktop
	fi
else
	if [ "$MB_LSB_ID" == Debian ]; then
		CHROMIUM_DESKTOP=chromium.debian-foreign.desktop
	else
		CHROMIUM_DESKTOP=chromium-foreign.desktop
	fi
fi
setup_launcher $i $CHROMIUM_DESKTOP

i="$((i+1))"
setup_launcher $i opera.desktop

i="$((i+1))"
setup_launcher $i utorrent.desktop

i="$((i+1))"
setup_launcher $i eclipse.desktop

i="$((i+1))"
setup_launcher $i gnome-system-monitor.desktop

i="$((i+1))"
setup_launcher $i disk-usage-analyzer.desktop

if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
	i="$((i+1))"
	setup_launcher $i disk-utility.desktop
else
	i="$((i+1))"
	setup_launcher $i gnome-disks.desktop
fi

i="$((i+1))"
setup_launcher $i synaptic.desktop

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'separator' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/expand" -t bool -s 'true' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/style" -t uint -s 0 -n

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'systray' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/show-frame" -t bool -s 'false' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/size-max" -t uint -s 28 -n

# Weather plugin
#i="$((i+1))"
#xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'weather' -n
#echo >~/.config/xfce4/panel/weather-$i.rc \
#'celcius=true
#loc_code=ROXX0003
#loc_name=Bucharest, Romania
#proxy_fromenv=false
#animation_transitions=false
#label0=289
#'

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'xkb-plugin' -n
echo >~/.config/xfce4/panel/xkb-plugin-$i.rc \
'display_type=1
group_policy=0
default_group=0
never_modify_config=false
model=pc105'
if [ -e "$MB_KB_LAYOUT_PATH" ]; then
	echo >>~/.config/xfce4/panel/xkb-plugin-$i.rc \
'layouts=mb,mb,us
variants=,umlaut,altgr-intl'
else
	echo "$MB_KB_LAYOUT_PATH" missing
	echo >>~/.config/xfce4/panel/xkb-plugin-$i.rc \
'layouts=ro,us
variants=,altgr-intl'
fi
echo >>~/.config/xfce4/panel/xkb-plugin-$i.rc \
'toggle_option=grp:shifts_toggle
compose_key_position=
'

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'datetime' -n
echo >~/.config/xfce4/panel/datetime-$i.rc \
"layout=2
date_font=`xfconf-query -c xsettings -p '/Gtk/FontName'`
time_font=Bitstream Vera Sans 8
date_format=%a %b %d
time_format=%H:%M
"

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'clock' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/show-frame" -t bool -s 'false' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/digital-format" -t string -s '%H:%M' -n

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'actions' -n


# top panel (default size is 30)
p="$((p+1))"
xfconf-query -c xfce4-panel -p "/panels/panel-$p/position" -t string -s 'p=6;x=0;y=0' -n
xfconf-query -c xfce4-panel -p "/panels/panel-$p/size" -t uint -s 30 -n
xfconf-query -c xfce4-panel -p "/panels/panel-$p/length" -t uint -s 100 -n
xfconf-query -c xfce4-panel -p "/panels/panel-$p/position-locked" -t bool -s 'true' -n

# xfconf-query -c xfce4-panel -p "/panels/panel-$p/plugin-ids" -n -t int -s 1 -t int -s 2 ...
TOP_PANEL_ARGS=""
for (( ; j <= i; j++ ))
do
	TOP_PANEL_ARGS="$TOP_PANEL_ARGS -t int -s $j"
done
xfconf-query -c xfce4-panel -p "/panels/panel-$p/plugin-ids" -n $TOP_PANEL_ARGS


# bottom panel plugins
i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'tasklist' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/show-handle" -t bool -s 'false' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/sort-order" -t uint -s 4 -n

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'separator' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/expand" -t bool -s 'true' -n
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i/style" -t uint -s 0 -n

i="$((i+1))"
xfconf-query -c xfce4-panel -p "/plugins/plugin-$i" -t string -s 'pager' -n


# bottom panel (default size is 40)
p="$((p+1))"
xfconf-query -c xfce4-panel -p "/panels/panel-$p/position" -t string -s 'p=10;x=0;y=0;' -n
xfconf-query -c xfce4-panel -p "/panels/panel-$p/size" -t uint -s 24 -n
xfconf-query -c xfce4-panel -p "/panels/panel-$p/length" -t uint -s 100 -n
xfconf-query -c xfce4-panel -p "/panels/panel-$p/position-locked" -t bool -s 'true' -n

# xfconf-query -c xfce4-panel -p "/panels/panel-$p/plugin-ids" -n -t int -s NR -t int -s NR ...
BOT_PANEL_ARGS=""
for (( ; j <= i; j++ ))
do
	BOT_PANEL_ARGS="$BOT_PANEL_ARGS -t int -s $j"
done
xfconf-query -c xfce4-panel -p "/panels/panel-$p/plugin-ids" -n $BOT_PANEL_ARGS


# reset panels (part 2: set number of panels)
PANEL_COUNT="$((p+1))"
xfconf-query -c xfce4-panel -p '/panels' -t uint -s $PANEL_COUNT -n


# restart xfce4-panel
nohup >/dev/null 2>/dev/null xfce4-panel &
