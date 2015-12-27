This is a small package for uploading the OpenNetHome firmware to a JeeLink Classic USB-Stick.
The firmware installation on the JeeLink has to be made from a Windows PC, but after that you can
use the JeeLink as a transmitter form OpenNetHome on Windows, Linux and OSX.On Linux and OSX you 
do not have to iinstall any serial drivers.

On Windows you first have to install the serial drivers for the JeeLink. If you have installed the
Arduino development environment, this is already installed and you should see a USB serial port in
the device manager when the JeeLink is plugged in.
If not, then you have to install the drivers under the drivers-folder in this directory.

After that you can install the OpenNetHome firmware on the JeeLink. You do that by running the
"InstallFirmware.bat" script in this folder from the command prompt with the JeeLink serial port as 
command argument, for example:

InstallFirmware.bat com7

This will upload the OpenNetHome firmware to the JeeLink.

Now you can start OpenNetHome, and create a JeeLink-Item from the Create/Edit Screen and configure the Serial port.
After that you should be able to control RF-Items with OpenNetHome via the JeeLink.