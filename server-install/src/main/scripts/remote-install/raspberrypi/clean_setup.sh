#!/bin/sh

ASK_INST_JAVA=N

install_java() {
  echo "Downloading and installing Java for Raspberry Pi"
  sudo apt-get update && sudo apt-get install oracle-java7-jdk
}

check_java() {
  echo "Checking Java version..."
  JAVA_VER=`echo "$(java -version 2>&1)" | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`

  if [ -n "$JAVA_VER" ]; then
    echo "Java found on this Raspberry Pi!"
  fi

  if [ -z "$JAVA_VER" ]; then
    echo "No java found on this Raspberry Pi!"
    ASK_INST_JAVA=Y
  fi

  if [ ! -z "$JAVA_VER" ]; then
    if [ "$JAVA_VER" -ge 16 ]; then
      echo "ok, java found and is 1.6 or newer"
    else
      echo "Sorry, your Java version is too old..."
      ASK_INST_JAVA=Y
    fi
  fi
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
  echo "Would have..."
else
  exit
fi

check_java

if [ "$ASK_INST_JAVA"=="Y" ]; then
  if [ $(ask_if "Do you wish to install/update Java? [Y/N]") -eq 1 ]; then
    install_java
  fi
fi

check_java
