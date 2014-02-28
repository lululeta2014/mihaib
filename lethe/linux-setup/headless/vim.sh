#!/bin/bash

set -u	# exit if using uninitialised variable
set -e	# exit if some command in this script fails
trap "echo $0 failed because a command in the script failed" ERR

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

source "$DIR/../sourceme.bash"


SYSTEM_VIMRC=/usr/share/vim/vimrc
KNOWN_VIMRC="$DIR/known_vimrc"
if [ "$MB_LSB_ID" == Debian ]; then
	KNOWN_VIMRC="$DIR/known_vimrc_debian"
fi

if ! diff -q "$SYSTEM_VIMRC" "$KNOWN_VIMRC"
then
	echo 'You might want to update this script'
fi

cp "$DIR/vimrc" ~/.vimrc
# put lilypond's dir at the start of vim's runtimepath,
# before ~/.vim/ftplugin/lilypond.vim which does "setlocal noshowmatch"
echo >>~/.vimrc \
"filetype off
execute 'set runtimepath=$MB_PRG_DIR/lilypond/lilypond/usr/share/lilypond/current/vim,'.&runtimepath
filetype on
"

# TODO /syntax or /indent ?
rm -rf ~/.vim
mkdir -p ~/.vim/syntax ~/.vim/indent ~/.vim/ftplugin ~/.vim/ftdetect

# JavaScript indentation:
#
# We need vim to indent this properly:
# f({
# a: 3,
# b: 4
# });
#
# for which we use the indent/javascript.vim file from this repository:
# http://www.vim.org/scripts/script.php?script_id=2765
# https://github.com/pangloss/vim-javascript
#
# We also want it to correctly match brackets here:
# function f() {
# /* { */
# }
# which we get from the matchit plugin.
#
# We don't include the syntax/javascript.vim file from this repo because
# in this example (which may be broken?) it highlights tokens in ‘type’ up to
# the closing ‘>’ then goes back to ‘comment-color’ and incorrectly matches
# the closing brace of the function to a brace from the comment.
# function f() {
# /**
#  * @type {function(): <{Object}>}
#  */
# }
# Brace matching outside comments must ignore comments.

# Put this at the top of the file. The javascript.vim we download exits on
# second load, ie when doing ":tabe b.js" and vim will insert tabs not spaces.
echo ":setlocal tabstop=8 softtabstop=4 shiftwidth=4 expandtab" \
>~/.vim/indent/javascript.vim

echo ":setlocal tabstop=4 softtabstop=4 shiftwidth=4 noexpandtab" \
>~/.vim/syntax/java.vim
echo ":setlocal tabstop=8 softtabstop=4 shiftwidth=4 expandtab" \
>~/.vim/syntax/python.vim
echo ":setlocal tabstop=8 softtabstop=2 shiftwidth=2 expandtab" \
>~/.vim/indent/html.vim
echo ":setlocal tabstop=8 softtabstop=2 shiftwidth=2 expandtab" \
>~/.vim/indent/php.vim
echo ":setlocal tabstop=8 softtabstop=2 shiftwidth=2 expandtab" \
>~/.vim/indent/lilypond.vim
echo "setlocal noshowmatch" \
>~/.vim/ftplugin/lilypond.vim
echo ":setlocal tabstop=8 softtabstop=2 shiftwidth=2 expandtab
:setlocal autoindent" \
>~/.vim/indent/svg.vim

# matchit script so ‘%’ doesn't match braces in comments
#mkdir -p ~/.vim/plugin ~/.vim/doc
#ln -s /usr/share/vim/vimcurrent/macros/matchit.vim ~/.vim/plugin
#ln -s /usr/share/vim/vimcurrent/macros/matchit.txt ~/.vim/doc

# http://go-lang.cat-v.org/text-editors/vim/
ln -s $MB_GOROOT/misc/vim/syntax/go.vim ~/.vim/syntax/
ln -s $MB_GOROOT/misc/vim/ftdetect/gofiletype.vim ~/.vim/ftdetect/
ln -s $MB_GOROOT/misc/vim/indent/go.vim ~/.vim/indent/
