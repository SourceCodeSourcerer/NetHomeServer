#!/bin/sh
rm nethomeservernightly.zip
wget http://wiki.nethome.nu/lib/exe/fetch.php/nethomeservernightly.zip
unzip nethomeservernightly.zip -d .
cd nethomeservernightly
chmod +x HomeManager_raspian.sh
ls -l
./HomeManager_raspian.sh
