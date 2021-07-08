# Installation for Gordian Signer

This document gives the instruction for installing the Gordian Signer Android

## Prerequisites
- Java 8

## Configuration
- The project uses [App Center](https://appcenter.ms) for the APK distribution so you have to provide the api key by creating the `app-center.properties` file at root dir follow the [sample](app-center.properties.sample)
- You have to create your `release.keystore` along with `release.properties` for the production signing. See [here](keystores/release.properties.sample) for the reference

## Dependencies
We use [Libwally Core](https://github.com/ElementsProject/libwally-core) and [BlockchainCommons UR Java library](https://github.com/jollyjoker992/bc-ur-java) as submodule so make sure you clone these repos correctly.

#### Install system dependencies
```console
./scripts/install_deps.sh
```
> It works smoothly in Linux but there is a need of root user permission if you are working on MacOS. You also have to give permission for the script to access `$HOME` dir for the NDK searching locally.

#### Install libwally-core
```console
./scripts/build-libwally-core.sh
```

#### Install BlockchainCommons UR Java library
```console
./scripts/build-bc-ur.sh
```

## Testing
```console
./gradlew clean testInhouseDebugUnitTest connectedInhouseDebugAndroidTest
```

## Building
```console
./gradlew clean -x test assembleInhouseDebug
```

## Distributing
```console
./gradlew appCenterUploadInhouseDebug
```