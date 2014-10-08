#!/bin/sh
mkdir /home/pi/noip
cd /home/pi/noip
wget http://www.no-ip.com/client/linux/noip-duc-linux.tar.gz
tar vzxf noip-duc-linux.tar.gz
cd noip-2.1.9-1
sudo make
sudo make install

# Assming above worked!
cd
sudo cp noip.sh /etc/init.d/noip
sudo chmod 755 /etc/init.d/noip

# Test by: sudo /etc/init.d/noip start | stop

sudo update-rc.d noip defaults



