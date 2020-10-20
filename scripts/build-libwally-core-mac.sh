#! /usr/bin/env bash

echo "Install dependencies"
DEPS=(swig libtool automake gnu-sed wget)

source $PWD/scripts/deps-helper.sh

for dep in "${DEPS[@]}"; do
  if brew ls --versions $dep >/dev/null; then
    echo "Package $dep already installed"
  else
    echo "Installing $dep..."
    echo y | brew install "$dep"
  fi
done

JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
echo "Java home: $JAVA_HOME"
if [ "$JAVA_HOME" == "" ]; then
  echo "Installing JDK..."
  pushd "$HOME" || exit
  install_java8_mac
  JAVA_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home"
  popd || exit
else
  echo "Java has been installed at $JAVA_HOME"
fi


if [ ! -f $JAVA_HOME/include/jni_md.h ]; then
  # Copy the jni_md.h to include dir
  sudo cp $JAVA_HOME/include/darwin/jni_md.h $JAVA_HOME/include
fi


echo "Checking NDK path..."
NDK_VERSION="r19c"
NDK_HOME=$HOME/android-ndk-$NDK_VERSION
NDK_PATH=$(check_ndk_path $NDK_VERSION)
echo "NDK path: $NDK_PATH"
if [ "$NDK_PATH" == "" ]; then
  echo "Installing NDK..."
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
export JAVA_HOME=$JAVA_HOME

echo "Building android libraries..."
pushd deps/libwally-core || exit
. tools/build_android_libraries.sh
popd || exit

echo "Copying release files..."
cp -r deps/libwally-core/release/src/swig_java/src/com app/src/main/java
mkdir -p app/src/main/libs/jni
cp -r deps/libwally-core/release/lib/* app/src/main/libs/jni
