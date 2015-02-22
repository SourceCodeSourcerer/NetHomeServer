NetHomeServer Linux installer

If you have a previous installation (NetHomeServer 2.0) that you want to move to this new daemon start,
you follow the these steps:

1) Install the new daemon by running the install script in this folder
2) Stop the new installed server by writing: sudo /etc/init.d/nethome stop
3) Copy the configuration file from the old installation to the new installation.
   The file is normally called "default.xml" and is located in the lib-folder of your old installation.
   Copy this file to /etc/opt/nethome/config.xml, and change owner to nethome, for example:

     sudo cp lib/default.xml /etc/opt/nethome/config.xml
     sudo chown nethome /etc/opt/nethome/config.xml

4) If you have any log-files for thermometers placed in the old installation,
   move those to /var/log/nethome/

     sudo cp lib/MyThermometer.log /var/log/nethome/MyThermometer.log
     sudo chown nethome /var/log/nethome/MyThermometer.log

5) Start the server again by writing: sudo /etc/init.d/nethome start
6) If you moved any log files, start the WEB gui and go in to the corresponding Items and change
   the log file names so they refer to the new file locations. If you just call it for example
   "MyThermometer.log", this will automatically refer to /var/log/nethome/MyThermometer.log, but you can also
   specify the full path in the Item like: "/var/log/nethome/MyThermometer.log"
7) Open the GUI-Item "JettyWEB" and change the MediaDirectory to /etc/opt/nethome/media

8) Restart the server with the command:

     sudo /etc/init.d/nethome restart

If you have installed the previous way of daemon start, you have to uninstall that first.
Do that by issuing the commands:

sudo nhs stop
sudo update-rc.d -f nhs-daemon remove
sudo rm /etc/init.d/nhs-daemon
sudo rm /usr/sbin/nhs

