#!/bin/sh
/sbin/modprobe ftdi_sio
echo 1781 0c31 > /sys/bus/usb-serial/drivers/ftdi_sio/new_id
