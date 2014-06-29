#!/bin/sh
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cp ${SCRIPTPATH}/98-nethome.rules /etc/udev/rules.d/
cp ${SCRIPTPATH}/ftdi_tellstick.sh /etc/
chmod +x /etc/ftdi_tellstick.sh
cp ${SCRIPTPATH}/ftdi_fhz1000.sh /etc/
chmod +x /etc/ftdi_fhz1000.sh
cp ${SCRIPTPATH}/ftdi_fhz1300.sh /etc/
chmod +x /etc/ftdi_fhz1300.sh
udevadm control --reload-rules
