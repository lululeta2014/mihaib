Usage: java -jar jencconv.jar [options] file [file2 file3 ...]
  or:  java -jar jencconv.jar
Convert files (file args interpretation depends on -r flag) or start GUI.

Options:
-h, --help	show this help message and exit
-e, --encodings	print known encodings and exit
-d, --detect  file
		detect possible encodings of file and exit
-i encoding	input encoding, your default is UTF-8
-o encoding	output encoding, default is UTF-8
-r, --replace	If present, overwrite each file (saving original to .bak).
		If absent, the files are interpreted as pairs
		(src1 dest1 src2 dest2...) and their number must be even.
-f, --force	With --replace, overwrite existing .bak files.
		Without --replace, overwrite existing destination files.
-p, --plugins	print available plugins and exit
-c, --chain  plugin1[#plugin2#plugin3]
		Chain specified plugins (parts of plugin class names).
		A name matching no plugin is re-checked case-insensitive.
		Exactly 1 plugin's fully qualified class name must match.
		For x.y, x.y.z: x is ambiguous, z and x.y aren't (full match).
