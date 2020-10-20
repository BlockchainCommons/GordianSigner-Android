# Install NDK
function install_ndk() {
  NDK_VERSION=$1
  FILE="android-ndk-$NDK_VERSION-linux-x86_64.zip"
  rm "$FILE"
  if [[ "$(uname)" == "Darwin" ]]; then
    FILE="android-ndk-$NDK_VERSION-darwin-x86_64.zip"
  fi
  wget -O "$FILE" -q "https://dl.google.com/android/repository/$FILE"
  unzip "$FILE" >/dev/null
}

# Check the existing NDK with given version if it already installed then return the absolute path
function check_ndk_path() {
  NDK_VERSION=$1
  echo $(find $HOME -type d -name "android-ndk-$NDK_VERSION" -print -quit 2>/dev/null)
}

# Install OpenJDK8 for Mac then return the default installation path
function install_java8_mac() {
  FILE=OpenJDK8U-jdk_x64_mac_hotspot_8u265b01.pkg
  rm "$FILE"
  wget -O "$FILE" -q "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/$FILE"
  sudo installer -pkg $FILE -target /
}
