# gnome-keyring (gkr) functions

function gkr-session-pid()
{
	# several users might be logged in, so xfce4-/gnome- session processes.
	# ‘gnome-session’ doesn't have the correct SSH_AUTH_SOCK but
	# gvfsd, gnome-panel, gconfd-2, dconf-service, gnome-screensaver do.
	local pid_list=`ps -C xfce4-session -C dconf-service -o pid --no-heading`
	local pid
	for pid in $pid_list
	do
		if [ $(ps --pid $pid -o user --no-heading) == $(whoami) ]; then
			echo $pid
			return
		fi
	done
}

function gkr-get-vars()
{
	# $commands=`gkr-get-vars`
	# # test return code ($? == 0) or "$commands" != "". If all well:
	# eval $commands
	#
	# If this function sets & exports the variables, a script using it can
	# 'source' this file then call the function and it will have the vars.
	# However this FAILS if your script is run by cron.
	# So this function outputs the commands and your script evals them.

	local pid=`gkr-session-pid`
	if [ ! $pid ]; then
		return 1
	fi

	local -a vars=( \
		DBUS_SESSION_BUS_ADDRESS \
		SSH_AUTH_SOCK \
		SSH_AGENT_PID \
		XDG_SESSION_COOKIE \
	)

	local var
	for var in ${vars[@]}; do
		local cmd
		cmd=$(sed 's/\x00/\n/g' /proc/$pid/environ | grep "^$var=")
		# cmd is of the form 'MY_VAR=value'
		echo $cmd
		echo export $var
	done

	return 0
}
