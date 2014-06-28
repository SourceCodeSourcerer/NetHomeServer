#!/bin/sh
#
#   This script downloads the nightly build of the NetHomeServer
#   and then unpacks it, and lastly runs the server in one go.
#

FILE=nethomeservernightly.zip

if [ -f $FILE ] ; then
    echo "Removing old file: $FILE"
    rm $FILE
fi

wget http://wiki.nethome.nu/lib/exe/fetch.php/$FILE
unzip $FILE -d .
cd nethomeservernightly
chmod +x HomeManager_raspian.sh
ls -l
./HomeManager_raspian.sh

