#!/bin/sh
# Taken from: http://www.rpiblog.com/2014/03/installing-oracle-jdk-8-on-raspberry-pi.html
sudo tar xvf jdk-8-*.tar -C /opt

# Set default java and javac to the new installed jdk8.
sudo update-alternatives --install /usr/bin/javac javac /opt/jdk1.8.0/bin/javac 1
sudo update-alternatives --install /usr/bin/java java /opt/jdk1.8.0/bin/java 1

sudo update-alternatives --config javac
sudo update-alternatives --config java

# After all, verify with the commands with -verion option.
java -version
javac -version
