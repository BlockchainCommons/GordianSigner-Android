#!/bin/bash

install_ndk() {
  NDK_VERSION=$1
  FILE="android-ndk-$NDK_VERSION-linux-x86_64.zip"
  if is_osx; then
    FILE="android-ndk-$NDK_VERSION-darwin-x86_64.zip"
  fi
  wget -O "$FILE" -q "https://dl.google.com/android/repository/$FILE"
  unzip "$FILE" >/dev/null
  rm -f "$FILE"
}

check_ndk_path() {
  NDK_VERSION=$1
  echo $(find $HOME -type d -name "android-ndk-$NDK_VERSION" -print -quit 2>/dev/null)
}

check_dep() {
  dep=$1
  echo $dep
  if is_osx; then
    if brew ls --versions "$dep" >/dev/null; then
      echo "Package '$dep' already installed"
    else
      echo "Installing '$dep'..."
      echo y | brew install "$dep"
    fi
  else
    if dpkg -s "$dep" >/dev/null; then
      echo "Package '$dep' already installed"
    else
      echo "Installing '$dep'..."
      echo y | apt-get install "$dep"
    fi
  fi
}

is_osx() {
  [[ "$(uname)" == "Darwin" ]]
}

install_java8_mac() {
  FILE=OpenJDK8U-jdk_x64_mac_hotspot_8u265b01.pkg
  wget -O "$FILE" -q "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/$FILE"
  sudo installer -pkg $FILE -target /
  rm -f "$FILE"
}
