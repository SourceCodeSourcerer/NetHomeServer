NetHomeServer Linux installer

The install.sh script in this directory will install NetHomeServer as a daemon
that will start automatically when the computer is booted.
The script must be run as root:

chmod +x install.sh
sudo ./install.sh

This will copy the NetHomeServer files to appropriate locations in the file system
and install it as a daemon running as the user nethome.
To stop the server you type:

sudo /etc/init.d/nethome stop

And to start it again you type:

sudo /etc/init.d/nethome start

The installation files are moved to the following locations:

Executable files -> /opt/nethome/
Configuration file -> /etc/opt/nethome
Media files -> /etc/opt/nethome/media
Log files -> /var/log/nethome
Start script -> /etc/init.d/nethome

To upgrade an existing installation installed this way, you simply run the upgrade script
in the new release:

chmod +x upgrade.sh
sudo ./upgrade.sh

This will preserve the current configuration and just upgrade the server files to the current version.

There is also a complete download/upgrade script which will download the latest nightly build of OpenNetHome and
upgrade the current installation. This script will also save a backup of each downloaded release, so you can
downgrade to an earlier release if you have problems after an upgrade. This script should be copied to your home
catalog and run from there when you want to upgrade your installation to the latest nightly build.

example:

cp upgrade_nethome_nightly.sh /home/pi/
cd /home/pi
chmod +x upgrade_nethome_nightly.sh
sudo ./upgrade_nethome_nightly.sh
