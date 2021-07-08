#!/bin/bash

source scripts/helper.sh

export ANDROID_NDK_HOME=$HOME/android-ndk-r19c

if [ "$ANDROID_SDK_ROOT" == "" ]; then
  if is_osx; then
    export ANDROID_SDK_ROOT=$HOME/Library/Android/sdk
  else
    export ANDROID_SDK_ROOT=$HOME/Android/Sdk
  fi
fi

if [ ! -d "$ANDROID_SDK_ROOT" ]; then
  echo 'Android sdk could not be found'
  exit 1
fi

echo 'Building bc-ur...'
pushd deps/bc-ur-java/android || exit
./gradlew clean assembleRelease
popd || exit

echo 'Copying bc-ur aar file...'
mkdir -p app/libs
cp -r deps/bc-ur-java/android/app/build/outputs/aar/app-release.aar app/libs
echo 'Done building bc-ur'
