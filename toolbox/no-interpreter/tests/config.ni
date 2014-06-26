# Full path to the System Under Test (or Java Main Class name)
executable:$TEXTTEST_ROOT/../no-interpreter.py

# From looking at the texttest sources:
# TextTest looks at the file extension to choose an interpreter (.jar, etc)
# and chooses ‘python’ for ‘.py’. From looking at the sources, found we can
# override it with this config option.
# From now on, programs will be able to use ‘no-interpreter’ as interpreter :)
# But don't use ‘no-interpreter’ recursively in its own tests, though.
interpreter:python3

# Naming scheme to use for files for stdin,stdout and stderr
filename_convention_scheme:standard

# Expanded name to use for application
full_name:No Interpreter

