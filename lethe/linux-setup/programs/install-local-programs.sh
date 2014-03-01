#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
SCRIPT_DIR=`dirname "$SCRIPT"`
source "$SCRIPT_DIR/../sourceme.bash"


# provide download-kit
# Since this file is ‘install-local-programs’, support offline mode
which download-kit >/dev/null || "$SCRIPT_DIR"/bootstrap-toolbox.sh

"$SCRIPT_DIR"/chromedriver.sh
"$SCRIPT_DIR"/java.sh
"$SCRIPT_DIR"/ant.sh
# Selenium libs for this repository; depends on Java and Ant
"$SCRIPT_DIR"/repo-selenium.sh
"$SCRIPT_DIR"/go.sh
"$SCRIPT_DIR"/opera.sh
"$SCRIPT_DIR"/notepad-plus-plus.sh
"$SCRIPT_DIR"/eclipse.sh
"$SCRIPT_DIR"/texttest.sh
"$SCRIPT_DIR"/lilypond.sh
"$SCRIPT_DIR"/appengine.sh
"$SCRIPT_DIR"/adobe-reader.sh
"$SCRIPT_DIR"/texlive.sh
