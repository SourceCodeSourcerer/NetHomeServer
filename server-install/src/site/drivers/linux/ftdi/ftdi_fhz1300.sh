#!/bin/sh
/sbin/modprobe ftdi_sio
echo 0403 e0e8 > /sys/bus/usb-serial/drivers/ftdi_sio/new_id
