#! /usr/bin/env bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

# use carefully: we're running as root, so whoami won't be useful
source "$DIR"/../sourceme.bash


cp "$DIR"/mihaib "$MB_KB_LAYOUT_PATH"

XML_FILE="$MB_KB_LAYOUT_DIR"/rules/evdev.xml
if grep -i mihaib "$XML_FILE"; then
	echo 'layout already present in' "$XML_FILE" '– not touching it'
else
	TO_INSERT='    <layout>
      <configItem>
        <name>'"$MB_KB_LAYOUT_NAME"'</name>

        <shortDescription>mb</shortDescription>
        <description>MihaiB</description>
        <languageList>
          <iso639Id>eng</iso639Id>
        </languageList>
      </configItem>
      <variantList>
        <variant>
	  <configItem>
	    <name>umlaut</name>
	    <description>MihaiB (Umlaut)</description>
	  </configItem>
	</variant>
      </variantList>
    </layout>'

	TO_INSERT=$(echo -n "$TO_INSERT" | tr '\n' '#')
	TO_INSERT=$(echo "$TO_INSERT" | sed -e 's/#/\\n/g')
	sed -i "$XML_FILE" \
		-e 's@^  <layoutList>$@  <layoutList>\n'"$TO_INSERT"'@'
fi


sed -i /etc/default/keyboard \
	-e 's/^XKBLAYOUT.*$/XKBLAYOUT="'"$MB_KB_LAYOUT_NAME"'"/' \
	-e 's/^XKBVARIANT.*$/XKBVARIANT=""/' \
	-e 's/^XKBOPTIONS.*$/XKBOPTIONS=""/'
dpkg-reconfigure keyboard-configuration
