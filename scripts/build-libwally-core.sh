#!/bin/bash

source scripts/helper.sh

export CC=clang
export ANDROID_NDK=$HOME/android-ndk-r19c

if is_osx; then
  java_home=$(/usr/libexec/java_home 2>/dev/null)
  export JAVA_HOME=$java_home
else
  if [ "$JAVA_HOME" == "" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
  fi
fi

echo "Building libwally-core..."
pushd deps/libwally-core || exit
. tools/build_android_libraries.sh
popd || exit

echo "Copying libwally-core release files..."
cp -r deps/libwally-core/release/src/swig_java/src/com app/src/main/java
mkdir -p app/src/main/libs/jni
cp -r deps/libwally-core/release/lib/* app/src/main/libs/jni
echo "Done building libwally-core"
