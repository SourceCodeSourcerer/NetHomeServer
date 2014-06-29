#!/bin/sh
/sbin/modprobe ftdi_sio
echo 0403 f06f > /sys/bus/usb-serial/drivers/ftdi_sio/new_id
