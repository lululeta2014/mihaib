#!/bin/bash

######## CHANGE THESE VARIABLES FOR YOUR SETUP

#### even an IP (like 127.0.0.1) works for SERVER_NAME
SERVER_NAME=$(sed -e 'y/ABCDEFGHIJKLMNOPQRSTUVWXYZ/abcdefghijklmnopqrstuvwxyz/' <<< $(hostname)).local
SERVER_PORT=9000
PHP_TIMEZONE=Europe/Athens

#### Set this to 1 to replace git:// with https://
LIBPHUTIL_URL=git://github.com/facebook/libphutil.git
ARCANIST_URL=git://github.com/facebook/arcanist.git
PHABRICATOR_URL=git://github.com/facebook/phabricator.git


set -u
set -e

SCRIPT_FILE=`readlink -f $0`
SCRIPT_DIR=`dirname "$SCRIPT_FILE"`
INSTALL_DIR=`pwd`

echo -n "Install to $INSTALL_DIR [Y/n] "
read OK
if [ "$OK" != "" -a "$OK" != y -a "$OK" != Y ]; then
	exit 0
fi

SRC_DIR="$INSTALL_DIR"/src
rm -rf "$SRC_DIR"
mkdir "$SRC_DIR"

KITS_DIR="$SCRIPT_DIR"/kits
#rm -rf "$KITS_DIR"
mkdir -p "$KITS_DIR"

HTTPD_LATEST=httpd-2.4.6.tar.bz2
HTTPD_PATTERN=httpd-*.tar.bz2
HTTPD_SRC_DIR="$INSTALL_DIR"/httpd-src
HTTPD_TARGET_DIR="$INSTALL_DIR"/httpd
download-kit \
	--file "$KITS_DIR"/"$HTTPD_LATEST" \
	--url http://archive.apache.org/dist/httpd/"$HTTPD_LATEST" \
	--delete-pattern "$HTTPD_PATTERN"

PHP_LATEST=php-5.5.4.tar.xz
PHP_PATTERN=php-*.tar.xz
PHP_SRC_DIR="$INSTALL_DIR"/php-src
PHP_TARGET_DIR="$INSTALL_DIR"/php
APXS2="$HTTPD_TARGET_DIR"/bin/apxs
download-kit \
	--file "$KITS_DIR"/"$PHP_LATEST" \
	--url http://www.php.net/distributions/"$PHP_LATEST" \
	--delete-pattern "$PHP_PATTERN"

MYSQL_SRV_DIR=MySQL-5.6
MYSQL_LATEST=mysql-5.6.14-linux-glibc2.5-x86_64.tar.gz
MYSQL_PATTERN=mysql-*-linux-glibc*-x86_64.tar.gz
MYSQL_DIR="$INSTALL_DIR"/mysql
download-kit \
	--file "$KITS_DIR"/"$MYSQL_LATEST" \
	--url http://cdn.mysql.com/Downloads/"$MYSQL_SRV_DIR"/"$MYSQL_LATEST" \
	--delete-pattern "$MYSQL_PATTERN"


echo -n 'This will DELETE httpd, php and mysql. Continue? [y/N] '
read OK
if [ "$OK" != y -a "$OK" != Y ]; then
	exit 0
fi

rm -rf "$HTTPD_SRC_DIR" "$HTTPD_TARGET_DIR" "$PHP_SRC_DIR" "$PHP_TARGET_DIR" "$MYSQL_DIR"


echo -n 'DELETE local Phabricator repos and clone them from github? [y/N] '
read OK
if [ "$OK" == y -o "$OK" == Y ]; then
	rm -rf "$INSTALL_DIR"/libphutil "$INSTALL_DIR"/arcanist \
		"$INSTALL_DIR"/phabricator
	for REPO_URL in $LIBPHUTIL_URL $ARCANIST_URL $PHABRICATOR_URL
	do
		git clone $REPO_URL
	done
	(
	cd "$INSTALL_DIR"/phabricator

	git submodule update --init
	)
fi


######## INSTALL APACHE & PHP

tar xvjf "$KITS_DIR"/$HTTPD_LATEST -C "$SRC_DIR"
mv "$SRC_DIR"/httpd-* "$HTTPD_SRC_DIR"
(
cd "$HTTPD_SRC_DIR"
./configure --prefix="$HTTPD_TARGET_DIR" --enable-so --enable-rewrite=shared
make
make install
)


tar xvJf "$KITS_DIR"/$PHP_LATEST -C "$SRC_DIR"
mv "$SRC_DIR"/php-* "$PHP_SRC_DIR"
(
cd "$PHP_SRC_DIR"
./configure --prefix="$PHP_TARGET_DIR" --with-apxs2="$APXS2" \
	--with-config-file-path="$PHP_TARGET_DIR" \
	--with-mysql=mysqlnd --with-openssl \
	--enable-mbstring --with-curl --enable-pcntl --with-gd
make
make install
)
cp "$PHP_SRC_DIR"/php.ini-development "$PHP_TARGET_DIR"/php.ini
sed -e 's|^;date.timezone =$|date.timezone = '$PHP_TIMEZONE'|' -i \
	"$PHP_TARGET_DIR"/php.ini


TARGET_HTTPD_CONF="$HTTPD_TARGET_DIR"/conf/httpd.conf
cp "$TARGET_HTTPD_CONF" "$TARGET_HTTPD_CONF.original"

sed \
-e 's/^Listen 80$/Listen '$SERVER_PORT'/' \
-e 's/^#ServerName www.example.com:80$/ServerName '$SERVER_NAME:$SERVER_PORT'/' \
-e 's|^DocumentRoot "'"$HTTPD_TARGET_DIR"/htdocs'"$|DocumentRoot "'"$INSTALL_DIR"/phabricator/webroot'"|' \
-e 's|^<Directory "'"$HTTPD_TARGET_DIR"/htdocs'">$|<Directory "'"$INSTALL_DIR"/phabricator/webroot'">|' \
-e 's|^    DirectoryIndex index.html$|    DirectoryIndex index.html index.php|' \
-e 's|^#LoadModule rewrite_module modules/mod_rewrite.so$|LoadModule rewrite_module modules/mod_rewrite.so|' \
-i "$TARGET_HTTPD_CONF"

echo >> "$TARGET_HTTPD_CONF" \
'<FilesMatch \.php$>
    SetHandler application/x-httpd-php
</FilesMatch>'

echo >> "$TARGET_HTTPD_CONF" \
'RewriteEngine on
RewriteRule ^/rsrc/(.*) - [L,QSA]
RewriteRule ^/favicon.ico - [L,QSA]
RewriteRule ^(.*)$ /index.php?__path__=$1 [L,QSA]'

#SetEnv PHABRICATOR_ENV custom/myconfig'

rm -rf "$HTTPD_SRC_DIR"
rm -rf "$PHP_SRC_DIR"


cat > apache-up <<EOF
#!/bin/bash
"$HTTPD_TARGET_DIR/bin/apachectl" start
EOF
chmod u+x apache-up

cat > apache-down <<EOF
#!/bin/bash
"$HTTPD_TARGET_DIR/bin/apachectl" stop
EOF
chmod u+x apache-down


######## INSTALL MYSQL

tar xvzf "$KITS_DIR"/"$MYSQL_LATEST" -C "$SRC_DIR"
mv "$SRC_DIR"/mysql-* "$MYSQL_DIR"

echo > "$MYSQL_DIR"/my-config-file \
"[client]
#socket		= /path/to/mysql/data/mysql.sock
user		= root
port		= 3306

[mysqld]
#socket		= mysql.sock
port		= 3306
pid-file	= mysql.pid
log-error	= mysql.err
basedir		= $MYSQL_DIR
datadir		= $MYSQL_DIR/data/"
(
cd "$MYSQL_DIR"
./scripts/mysql_install_db --no-defaults --basedir=. --datadir=./data
)

cat > mysql-up <<EOF
#!/bin/bash

"$MYSQL_DIR/bin/mysqld" --defaults-file="$MYSQL_DIR/my-config-file"
EOF
chmod u+x mysql-up

cat > mysql-down <<EOF
#!/bin/bash

"$MYSQL_DIR/bin/mysqladmin" --defaults-file="$MYSQL_DIR/my-config-file" shutdown
EOF
chmod u+x mysql-down

cat > mysql-connect <<EOF
#!/bin/bash

"$MYSQL_DIR/bin/mysql" --defaults-file="$MYSQL_DIR/my-config-file"
EOF
chmod u+x mysql-connect


######## PHB START AND STOP SCRIPTS
cat >run-with-php.sh <<EOF
#!/usr/bin/env bash
PATH="$PHP_TARGET_DIR/bin":\$PATH "\$@"
EOF
chmod u+x run-with-php.sh

echo > up-phb \
'#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

"$DIR"/mysql-up &
"$DIR"/apache-up
sleep 5
"$DIR"/phd-up'
chmod u+x up-phb

echo > down-phb \
'#!/bin/bash

SCRIPT=`readlink -f "$0"`
DIR=`dirname "$SCRIPT"`

"$DIR"/phd-down
"$DIR"/apache-down
"$DIR"/mysql-down'
chmod u+x down-phb


cat > phd-status <<EOF
#!/bin/bash
PATH="$PHP_TARGET_DIR/bin":\$PATH "$INSTALL_DIR/phabricator/bin/phd" status
EOF
chmod u+x phd-status

cat > phd-up <<EOF
#!/bin/bash
PATH="$PHP_TARGET_DIR/bin":\$PATH "$INSTALL_DIR/phabricator/bin/phd" start
EOF
chmod u+x phd-up

cat > phd-down <<EOF
#!/bin/bash
PATH="$PHP_TARGET_DIR/bin":\$PATH "$INSTALL_DIR/phabricator/bin/phd" stop
EOF
chmod u+x phd-down

cat > phb-update <<EOF
#!/bin/bash
"$INSTALL_DIR/down-phb"

cd "$INSTALL_DIR/libphutil"; git pull
cd "$INSTALL_DIR/arcanist"; git pull
cd "$INSTALL_DIR/phabricator"; git pull
git submodule update --init

"$INSTALL_DIR"/mysql-up &
sleep 5
"$INSTALL_DIR"/run-with-php.sh "$INSTALL_DIR"/phabricator/bin/storage upgrade
"$INSTALL_DIR"/mysql-down
EOF
chmod u+x phb-update

echo "Running ./phb-update should do the DB initialization"
