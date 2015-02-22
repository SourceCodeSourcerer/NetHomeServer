#!/bin/sh
#
#Assumes root, make sure to call as 'sudo ./uninstall.sh'
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root. Call as 'sudo $0'" 1>&2
   exit 1
fi

read -p "Do you wish to remove OpenNetHome and all its configuration files? (y/N)" yn
case $yn in
    [Yy]* ) break;;
    [Nn]* ) echo "Cancelling uninstallation"; exit;;
    * ) echo "Cancelling uninstallation"; exit;;
esac

INSTALLATION_ROOT=/opt/nethome
CONFIGURATION_ROOT=/etc/opt/nethome
LOG_ROOT=/var/log/nethome
PID_ROOT=/var/run/nethome

echo "Stopping Server"
update-rc.d /etc/init.d/nethome	remove
/etc/init.d/nethome stop
echo "Removing installed files"
rm /etc/init.d/nethome
rm -Rf $INSTALLATION_ROOT
cp $CONFIGURATION_ROOT/config.xml /home/pi/old_config.xml
rm -Rf $CONFIGURATION_ROOT
rm -Rf $LOG_ROOT
rm -Rf $PID_ROOT
rm -Rf /home/nethome
