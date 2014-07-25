# source this from the other scripts

# If this script has not already been sourced (var is undefined), run commands.
#
# Make sure to EXPORT all vars you want to use after sourcing this, otherwise:
# 1) main script sources this file; can acces exported and non-exported vars
# 2) main script runs child script, which can access only exported vars
# 3) child sources this, commands DON'T run, it's left with what parent exports
#
# So don't leave ANY unexported vars, or your scripts might work differently
# when you run them directly vs. when they're run by another main script.
if [ ! -v MB_SOURCEME_BASH ]; then

	MB_SOURCEME_BASH=true
	export MB_SOURCEME_BASH

	MB_HOSTNAME=`uname -n`
	MB_WHOAMI=`whoami`
	export MB_HOSTNAME MB_WHOAMI

	MB_LSB_ID=`lsb_release -is`	# Distrib. ID	(Ubuntu, Debian)
	MB_LSB_REL=`lsb_release -rs`	# release	(12.04, testing)
	MB_LSB_CN=`lsb_release -cs`	# codename	(precise, wheezy)
	export MB_LSB_ID MB_LSB_REL MB_LSB_CN

	if [ "$MB_HOSTNAME" == 'Castor' -o "$MB_HOSTNAME" == 'Hermes' ]; then
		MB_PRIVATE_COMP=1
		MB_DESKTOP_DIR=/mnt/eph/Desktop
	else
		MB_PRIVATE_COMP=0
		MB_DESKTOP_DIR=/mnt/haven/Desktop
	fi
	export MB_PRIVATE_COMP MB_DESKTOP_DIR

	MB_PRG_DIR=/mnt/haven/etc/prg-auto
	MB_BYHAND_PRG_DIR=/mnt/haven/etc/byhand-prg
	MB_KITS_DIR=/mnt/haven/etc/kits-auto
	export MB_PRG_DIR MB_BYHAND_PRG_DIR MB_KITS_DIR

	if [ "$MB_PRIVATE_COMP" == 1 ]; then
		MB_REPOS_DIR=/mnt/haven/repos
		export MB_REPOS_DIR
	fi

	MB_MYSELF=false
	if [ "$MB_PRIVATE_COMP" == 1 -a "$MB_WHOAMI" == mihai ]; then
		MB_MYSELF=true
	fi
	### fork MB_MYSELF start ###
	### fork MB_MYSELF end ###
	export MB_MYSELF

	MB_LINK_DESKTOP="$MB_MYSELF"
	export MB_LINK_DESKTOP	# whether to link ~/Desktop to $MB_DESKTOP_DIR

	MB_BROWSER_WIDTH=1024
	MB_BROWSER_HEIGHT=2000 # assuming browser trims it to screen height
	export MB_BROWSER_WIDTH MB_BROWSER_HEIGHT

	if [ "$MB_PRIVATE_COMP" == "1" -a "$MB_MYSELF" == "true" ]; then
		MB_SSH_DIR=/mnt/haven/craft/misc/ssh
		export MB_SSH_DIR

		MB_CHECKGMAIL_USERS_FILE=/mnt/haven/craft/misc/mb-checkgmail-users
		MB_BROWSER_GMAIL_USERS_FILE=/mnt/haven/craft/misc/mb-browser-gmail-users
		export MB_CHECKGMAIL_USERS_FILE MB_BROWSER_GMAIL_USERS_FILE

		MB_ONLINE_BACKUP_FILE=/mnt/haven/craft/misc/mb-online-backup.json
		export MB_ONLINE_BACKUP_FILE

		# Define MB_VBOX_VMS_DIR, MB_VAGRANT_HOME only for MB_MYSELF
	fi

	### fork SSH, Gmail, online backup, VBox, Vagrant start ###
	### fork SSH, Gmail, online backup, VBox, Vagrant end ###

	MB_BROWSER_ALT_PROFILE=lowmen
	# don't conflict with the default user-wide ‘.opera’
	MB_OPERA_PREFS_ROOT=~/.opera-mihaib
	MB_OPERA_REG_PROF_PATH=$MB_OPERA_PREFS_ROOT/regular-profile
	MB_OPERA_ALT_PROF_PATH=$MB_OPERA_PREFS_ROOT/"$MB_BROWSER_ALT_PROFILE"-profile

	export MB_BROWSER_ALT_PROFILE
	export MB_OPERA_PREFS_ROOT
	export MB_OPERA_REG_PROF_PATH MB_OPERA_ALT_PROF_PATH

	MB_TOOLS_DEST=~/.mb-tools
	export MB_TOOLS_DEST

	MB_GOROOT=$MB_PRG_DIR/go
	MB_GOPATH_DIR=$MB_PRG_DIR/gopath
	export MB_GOROOT MB_GOPATH_DIR

	MB_KB_LAYOUT_DIR=/usr/share/X11/xkb
	MB_KB_LAYOUT_NAME=mb
	MB_KB_LAYOUT_PATH="$MB_KB_LAYOUT_DIR"/symbols/"$MB_KB_LAYOUT_NAME"
	export MB_KB_LAYOUT_DIR MB_KB_LAYOUT_NAME MB_KB_LAYOUT_PATH

	MB_SCREENSAVER_MINS=30
	### fork MB_SCREENSAVER_MINS start ###
	### fork MB_SCREENSAVER_MINS end ###
	export MB_SCREENSAVER_MINS

	if [ "$MB_HOSTNAME" == "Hermes" ]; then
		MB_DPI=110
	elif [ "$MB_HOSTNAME" == "Castor" ]; then
		MB_DPI=90
	fi
	### fork MB_DPI, MB_ALT_DPI start ###
	### fork MB_DPI, MB_ALT_DPI end ###
	export MB_DPI


	# desktop background
	if [ "$MB_MYSELF" == true ]; then
		if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-wheezy' ]; then
			if [ $MB_PRIVATE_COMP == 1 ]; then
				MB_WALLPAPER_FILE=/usr/share/backgrounds/gnome/Terraform-orange.jpg
			fi
			### fork start ###
			### fork end ###
		fi
		if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-14.04' ]; then
			if [ $MB_HOSTNAME == "Hermes" ]; then
				MB_WALLPAPER_FILE=/usr/share/backgrounds/gnome/Terraform-green.jpg
			fi
			### fork start ###
			### fork end ###
		fi
		if [ "$MB_LSB_ID"-"$MB_LSB_REL" == 'Ubuntu-13.10' ]; then
			if [ "$MB_HOSTNAME" == "Hermes" ]; then
				MB_WALLPAPER_FILE=/usr/share/backgrounds/Thingvellir_by_pattersa.jpg
			fi
			if [ "$MB_HOSTNAME" == "Castor" ]; then
				MB_WALLPAPER_FILE=/usr/share/backgrounds/Savannah_Lilian_Blot_by_a_Blot_on_the_landscape.jpg
			fi
			### fork start ###
			### fork end ###
		fi
		if [ "$MB_LSB_ID"-"$MB_LSB_CN" == 'Debian-jessie' ]; then
			if [ "$MB_HOSTNAME" == "Hermes" ]; then
				MB_WALLPAPER_FILE=/usr/share/backgrounds/gnome/Dark_Ivy.jpg
			fi

			if [ "$MB_HOSTNAME" == "Castor" ]; then
				MB_WALLPAPER_FILE=/usr/share/backgrounds/gnome/Dark_Ivy.jpg
			fi
			### fork start ###
			### fork end ###
		fi
	fi
	export MB_WALLPAPER_FILE


	# You may also extend this script in your fork below this line

fi
