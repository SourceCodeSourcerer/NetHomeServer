#!/bin/sh
#
#Assumes root, make sure to call as 'sudo ./install.sh'
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root. Call as 'sudo $0'" 1>&2
   exit 1
fi

#
SRCPATH=$(dirname $(readlink -f $0))
SRCROOT=$SRCPATH/../..
INSTALLATION_ROOT=/opt/nethome
CONFIGURATION_ROOT=/etc/opt/nethome
LOG_ROOT=/var/log/nethome
PID_ROOT=/var/run/nethome
PID_FILE=$PID_ROOT/nethome.pid
NH_GROUP=nethome
NH_USER=nethome

# Check so there really is an existing installation
if [ ! -x "/etc/init.d/nethome" -o ! -d "$INSTALLATION_ROOT" -o ! -d "$CONFIGURATION_ROOT" -o ! -d "/home/nethome" ]; then
  echo "Cannot find a complete installation to upgrade. The server must be installed with the installation script found in the same folder as this script."
  exit 1
fi

# Stop the server
echo "Stopping server" 1>&2
/etc/init.d/nethome stop

# Install a new lib directory
echo "Upgrading files" 1>&2
chmod -f +w $INSTALLATION_ROOT/lib_backup
rm -rf $INSTALLATION_ROOT/lib_backup
mv $INSTALLATION_ROOT/lib $INSTALLATION_ROOT/lib_backup
cp -r $SRCROOT/lib $INSTALLATION_ROOT/lib
rm -f $INSTALLATION_ROOT/lib/librxtxSerial.so
cp $INSTALLATION_ROOT/os/librxtxSerial_raspian.so $INSTALLATION_ROOT/lib/librxtxSerial.so
chmod -w $INSTALLATION_ROOT/lib

# Added upgrade tasks
cp -f $SRCPATH/nethome /etc/init.d
ln -f /etc/init.d/nethome /usr/sbin/nethome
if [ -f /home/nethome/upgrade_nethome_nightly.sh ]; then
  mv -f /home/nethome/upgrade_nethome_nightly.sh /home/nethome/upgrade_nethome_nightly_old.sh
fi
cp $SRCPATH/upgrade_nethome_nightly.sh /home/nethome/upgrade_nethome_nightly.sh
chmod +x /home/nethome/upgrade_nethome_nightly.sh

# Start the server
echo "Restarting server" 1>&2
/etc/init.d/nethome start
echo "Upgrade complete" 1>&2