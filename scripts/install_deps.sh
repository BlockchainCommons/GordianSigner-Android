#!/bin/bash

source scripts/helper.sh

deps=(swig libtool make automake wget)

if is_osx; then
  deps+=(gnu-sed)
else
  deps+=(clang openjdk-8-jdk)
fi

echo "Checking and installing dependencies '${deps[*]}'..."
apt-get update
for dep in "${deps[@]}"; do
  check_dep $dep
done

# Check and install java 8 for macOS
if is_osx; then
  java_version=$(java -version 2>&1 | awk -F '"' '// {print $2}')
  if [[ $java_version != "1.8.0_265" ]]; then
    echo "Installing JDK 8..."
    install_java8_mac || exit
  else
    echo "JDK 8 has been installed at $(/usr/libexec/java_home 2>/dev/null)"
  fi
fi

echo "Checking NDK path..."
ndk_version="r19c"
ndk_path=$(check_ndk_path $ndk_version)
if [ "$ndk_path" == "" ]; then
  echo "Installing NDK..."
  pushd "$HOME" || exit
  install_ndk $ndk_version
  popd || exit
else
  echo "NDK has been installed at $ndk_path"
fi
