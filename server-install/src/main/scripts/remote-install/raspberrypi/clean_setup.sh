#!/bin/sh

PI_USER_HOME_DIR=$(pwd)
ASK_INST_JAVA=N
NOIP_DIR=/home/pi/noip
GITHUB_URL=https://raw.githubusercontent.com/SourceCodeSourcerer/NetHomeServer/master
NHS_SCRIPTS=server-install/src/main/scripts
NHS_NIGHTLY_PATH=nethomeservernightly
NHS_NIGHTLY_FILE=nethomeservernightly.zip
NHS_LAUNCH_SCRIPTS=server-install/src/main/scripts/

install_java() {
  echo "Downloading and installing Java for Raspberry Pi"
  # ASSUMING RUNNING AS SUDO
  apt-get update && apt-get install oracle-java7-jdk
}


check_java() {
  echo "Checking Java version..."
  JAVA_VER=`echo "$(java -version 2>&1)" | grep "java version" | awk '{ print substr($3, 4, 1); }'`

  if [ -n "$JAVA_VER" ]; then
    echo "Java found on this Raspberry Pi!"
  fi

  if [ -z "$JAVA_VER" ]; then
    echo "No java found on this Raspberry Pi!"
    ASK_INST_JAVA=Y
  fi

  if [ ! -z "$JAVA_VER" ]; then
    if [ "$JAVA_VER" -ge 6 ]; then
      echo "ok, java found and is 1.6 or newer"
    else
      echo "Sorry, your Java version is too old ($JAVA_VER)..."
      ASK_INST_JAVA=Y
    fi
  fi
}


get_nhs() {
  if [ $(ask_if "Do you want to download the latest nightly build of NetHomeServer? [Y/N]") -eq 0 ]; then
    echo "Skipped downloading NetHomeServer."
    return
  fi

  echo "Downloading NetHomeServer..."

  # Start at users home directory
  cd $PI_USER_HOME_DIR

  if [ -f $NHS_NIGHTLY_FILE ] ; then
    echo "Removing old file: $NHS_NIGHTLY_FILE"
    rm $NHS_NIGHTLY_FILE
  fi

  wget http://wiki.nethome.nu/lib/exe/fetch.php/$NHS_NIGHTLY_FILE
  unzip $NHS_NIGHTLY_FILE -d .
  chmod +x $NHS_NIGHTLY_PATH/HomeManager_raspian.sh
}


setup_nhs_boot() {
  if [ $(ask_if "Do you want to setup so that NetHomeServer automatically starts on boot? [Y/N]") -eq 0 ]; then
    echo "Skip setup of NetHomeServer start on boot."
    return
  fi

  # Since we are running as SUDO (root) we can't just do a simple "cd" cause we end up in /root
  cd $PI_USER_HOME_DIR

  if [ ! -d $NHS_NIGHTLY_PATH ]; then
    echo "Nightly NetHomeServer directory ($NHS_NIGHTLY_PATH) is missing. Can't continue!"
    pwd
    ls -la
    return
  fi

  cd $NHS_NIGHTLY_PATH

  # Maybe should download install_daemon.sh from GITHUB instead?
  if [ ! -f install_daemon.sh ]; then
    echo "Missing necessary install_daemon.sh script. Can't continue!"
    ls -la
    return
  fi

  chmod +x install_daemon.sh
  sh ./install_daemon.sh
  wait

  echo "Done installing daemon!"
  cd $PI_USER_HOME_DIR
}


download_noip() {
  echo "Downloading no-ip software..."

  mkdir $NOIP_DIR
  cd $NOIP_DIR
  wget http://www.no-ip.com/client/linux/noip-duc-linux.tar.gz
  tar vzxf noip-duc-linux.tar.gz

  NOIP_VERSION=noip-2.1.9-1

  if [ ! -d $NOIP_VERSION ]; then
    echo "ERROR: Missing expected $NOIP_VERSION directory - can't continue installation!"
    return
  fi

  cd $NOIP_VERSION
  # ASSUMING RUNNING AS SUDO
  make
  make install

  # Assming above worked, go back to user's directory
  cd $PI_USER_HOME_DIR
}

setup_noip() {
  if [ $(ask_if "Do you want to download and setup NO-IP support? [Y/N]") -eq 0 ]; then
    echo "Skip setup of no-ip."
    return
  fi

  if [ -d $NOIP_DIR ]; then
    if [ $(ask_if "NO-IP seem to exist already. Do you want to install it again? [Y/N]") -eq 1 ]; then
      download_noip
    fi
  else
    download_noip
  fi

  rm noip.sh*
  echo "Downloading noip.sh script from GITHUB..."
  wget $GITHUB_URL/$NHS_SCRIPTS/remote-install/raspberrypi/noip.sh

  if [ ! -f noip.sh ]; then
    echo "ERROR: Missing expected noip.sh script - can't continue installation!"
    return
  fi

  # Copy script into place and make it executable
  # ASSUMING RUNNING AS SUDO
  cp noip.sh /etc/init.d/noip
  chmod 755 /etc/init.d/noip

# Test by: sudo /etc/init.d/noip start | stop

  # Install startup script
  # ASSUMING RUNNING AS SUDO
  update-rc.d noip defaults
}


setup_avahi() {
  # AVAHI: This isn't really needed!
  return
  echo "Setting up avahi-daemon so that this raspberry pi name is static on reboot"
  # ASSUMING RUNNING AS SUDO
  apt-get install avahi-daemon
}

setup_tellstick() {
  # See: http://wiki.nethome.nu/doku.php/installtellstickdriverlinux?s[]=tellstick

  # ASSUMING RUNNING AS SUDO
  cp $NHS_NIGHTLY_PATH/drivers/linux/ftdi/98-nethome.rules /etc/udev/rules.d/
  cp $NHS_NIGHTLY_PATH/drivers/linux/ftdi/ftdi_tellstick.sh /etc/
  cp $NHS_NIGHTLY_PATH/drivers/linux/ftdi/ftdi_fhz1000.sh /etc/
  cp $NHS_NIGHTLY_PATH/drivers/linux/ftdi/ftdi_fhz1300.sh /etc/

  chmod +x /etc/ftdi_tellstick.sh
  chmod +x /etc/ftdi_fhz1000.sh
  chmod +x /etc/ftdi_fhz1300.sh

  udevadm control --reload-rules
}

ask_if() {
  while true; do
    read -p "$1" yn
    case $yn in
      [Yy]* ) echo 1; exit;;
      [Nn]* ) echo 0; exit;;
      * ) echo "Please answer yes or no.";;
    esac
  done
}


echo "======================================================================="
echo "Please answer these questions to have your Raspberry Pi set up running"
echo "  a NetHomeServer configuration."
if [ $(ask_if "Do you want to continue? [Y/N]") -eq 1 ]; then
  echo ""
else
  echo "Cancelled setup!"
  exit
fi

check_java

if [ $ASK_INST_JAVA = 'Y' ]; then
  if [ $(ask_if "Do you wish to install/update Java? [Y/N]") -eq 1 ]; then
    install_java
    check_java
  fi
fi

get_nhs

setup_noip

setup_avahi

setup_nhs_boot

