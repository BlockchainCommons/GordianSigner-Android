#! /usr/bin/env bash

echo "Install dependencies"
DEPS=(clang swig libtool automake openjdk-8-jdk wget)

apt-get update
source $PWD/scripts/deps-helper.sh

for dep in "${DEPS[@]}"; do
  if [[ $(dpkg -s $dep 2>/dev/null) -ne "" ]]; then
    echo "Package $dep already installed"
  else
    echo "Installing $dep..."
    echo y | apt-get install "$dep"
  fi
done

echo "Checking NDK path..."
NDK_VERSION="r19c"
NDK_HOME=$HOME/android-ndk-$NDK_VERSION
NDK_PATH=$(check_ndk_path $NDK_VERSION)
echo "NDK path: $NDK_PATH"
if [ "$NDK_PATH" == "" ]; then
  echo "Installing NDK"
  pushd "$HOME" || exit
  install_ndk $NDK_VERSION
  popd || exit
else
  echo "NDK has been installed at $NDK_PATH"
  NDK_HOME=$NDK_PATH
fi

echo "Set env variables"
export CC=clang
export ANDROID_NDK=$NDK_HOME
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

echo "Building android libraries..."
pushd deps/libwally-core || exit
. tools/build_android_libraries.sh
popd || exit

echo "Copying release files..."
cp -r deps/libwally-core/release/src/swig_java/src/com app/src/main/java
mkdir -p app/src/main/libs/jni
cp -r deps/libwally-core/release/lib/* app/src/main/libs/jni
