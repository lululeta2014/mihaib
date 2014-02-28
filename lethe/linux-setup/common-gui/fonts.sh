#! /usr/bin/env bash

FC_DIR=~/.config/fontconfig
mkdir -p "$FC_DIR"
cat >"$FC_DIR"/fonts.conf <<"EOF"
<?xml version="1.0"?>
<!DOCTYPE fontconfig SYSTEM "fonts.dtd">
<fontconfig>
<alias>
	<family>monospace</family>
	<prefer><family>DejaVu Sans Mono</family></prefer>
</alias>
</fontconfig>
EOF
