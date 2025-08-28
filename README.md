# Nabto client sdk for Android

This repository contains the Java and Kotlin wrapper libraries for the Nabto
Edge Client SDK and integration code for the Nabto IAM system.

The folder library contains a wrapper for the Nabto Edge Client SDK library.

## Android Nabto Edge Client SDK Library

This library consists of a Java Wrapper and JNI code for the native Nabto Edge
Client SDK library.

## Release strategy

All non-tagged builds are put into the snapshot repository at https://central.sonatype.com/service/rest/repository/browse/maven-snapshots using a naming strategy which is `<branch-name>-SNAPSHOT`

To consume snapshot builds in your project, add the following repository to your gradle settings

```kotlin
maven {
    name = "Central Portal Snapshots"
    url = "https://central.sonatype.com/repository/maven-snapshots/"
}
```

Read more at https://central.sonatype.org/publish/publish-portal-snapshots/#consuming-snapshot-releases-for-your-project

## Building the library

Run `./build-scripts/android.sh` this will build everything.

In a development setting it is better to build the Nabto Edge Client JNI
libraries seperately and then invoke appropriate gradle commands

e.g.
```
./build-scripts/android.sh build_native_libraries
./gradlew library:build
```

# Testing

## Test library and library-ktx

Run tests on a single running device

```
./gradlew library:connectedAndroidTest
./gradlew library-ktx:connectedAndroidTest

```

Run tests on a predefined group of devices
```
./gradlew library:phonesGroupCheck
```

Mdns and Local Connections is not tested using the connectedAndroidTest projects, they need to be tested using the manual-tests test suite.

Run a local mdns test device and switch the phone to the local wifi, the phones screen needs to be turned on else the wifi will probably be in a low power state and mdns packets are not receieved.
```
./start_local_test_devices.sh
```

```
./gradlew :manual-tests:connectedAndroidTest
```

Run a specific test ./gradlew :manual-tests:connectedAndroidTes-Pandroid.testInstrumentationRunnerArguments.class=com.nabto.edge.client.test.MdnsTest or ConnectionTest



### Test IAM-Util

```
./gradlew iam-util:test
```

We have quite a few iam tests which requires running local test devices.

```
./start_local_test_devices.sh
```

```
./gradlew :iam-util:connectedAndroidTest
```

## Run connectedAndroidTests from an apk on a device

Run the manualtests:
```
$ adb install ~/Downloads/manual-tests-debug-androidTest.apk
...
# Ensure that the screen is turned on, else mdns fails
$ adb shell am instrument -w com.nabto.edge.client.manualtests.test/androidx.test.runner.AndroidJUnitRunner

com.nabto.edge.client.manualtests.ConnectionTest:..
com.nabto.edge.client.manualtests.MdnsTest:...

Time: 3,29

OK (5 tests)
```

Run the library tests
```
$ adb install ~/Downloads/library-debug-androidTest\ \(1\).apk
...
$ adb shell am instrument -w com.nabto.edge.client.test/androidx.test.runner.AndroidJUnitRunner

com.nabto.edge.client.manualtests.ConnectionTest:..
com.nabto.edge.client.manualtests.MdnsTest:...

Time: 3,29

OK (5 tests)
```

# Deploying to Maven Central with JReleaser

This repository now uses JReleaser to upload artifacts to Maven Central. This happens in two steps
1. The publish step runs the `scripts/publish.gradle` in each subproject to store staging artifacts in the build directory.
2. JReleaser (configured in `scripts/jreleaser.gradle`) deploys the staging artifacts to Maven Central using the new Portal API.

[More info about JReleaser's workflow.](https://jreleaser.org/guide/latest/concepts/workflow.html) Currently we only use the `deploy`, and `sign` steps.

Sonatype has sunset OSSRH and now intends for developers to use their new Portal Publisher API. [As of writing there is no official Gradle plugin for publishing using the Portal API](https://central.sonatype.org/publish/publish-portal-gradle/). JReleaser is used to smoothly enable a transition to the Portal API.

## Gradle properties for deployment

The following gradle properties must be set to allow JReleaser to publish artifacts.

* `GPG_SIGNING_KEY_BASE64` Base64 encoded armored PGP private key
* `GPG_SIGNING_PUBLIC_KEY_BASE64` Base64 encoded armored PGP public key
* `GPG_SIGNING_PASSWORD` Passphrase for the above keypair
* `OSSRH_USERNAME` **IMPORTANT** this is no longer the OSSRH username but actually the username of a token generated in the account options in https://central.sonatype.com/
* `OSSRH_PASSWORD` Similar to above, this is the password of that same token.

Note that the signing keys are **armored and base64 encoded**, so they are actually twice-encoded. This is to get around a Jenkins limitation where secrets cannot have newlines. To output these keys from `gpg` you can use the following commands
```
gpg --armor --export <KEYID> | base64 -w 0
gpg --armor --export-secret-keys <KEYID> | base64 -w 0
```

## Publishing staging artifacts

```
./gradlew publish
```

Running publish will no longer attempt to deploy to maven. Instead it will store staging artifacts into the `build/staging-deploy` directory.

## Deploying

```
./gradlew jreleaserDeploy
```
Running this command will deploy the files in `build/staging-deploy` to Maven Central.

If the current Git commit is tagged with a version it will be uploaded but not published. You must manually go to https://central.sonatype.com/ and publish the staged artifacts.

If there is no tag JReleaser will only deploy a snapshot.

---

## Debugging swig generation

`swig -c++ -java -debug-tmsearch nabto_client.i`, it can also be
appropriate to make a minimum reproduction and get the swig right in a
limited example.

## Working with android stack traces

```
./Android/Sdk/platform-tools/adb logcat > out.txt
./Android/Sdk/ndk-bundle/ndk-stack -sym sandbox/nabto-client-sdk/android-client/jni/build/intermediates/merged_native_libs/debug/out/lib/arm64-v8a/ -dump out.txt
```

## Open a remote pairing fragment

```
adb shell am start -W -a android.intent.action.VIEW -d 'heatpump://app/remote_pair_device?product_id=pr-bqyh43fb\&device_id=de-hkpcwynp\&server_key=sk-9d3028f1f3de68f0844ea544a42a4f05\&server_url=https://a.clients.dev.nabto.com' com.nabto.edge.heatpump
```


# QA needed for a library

## Lifecycle tests

The idea with these tests is that they should test app lifecycle events.

### Test Life 1.

An app should be able to handle activity lifecycle events described here:
https://developer.android.com/guide/components/activities/activity-lifecycle

  1. The app should be able to start and then be stopped.
  2. The app should be able to be paused and then resumed.
  3. The app should be able to be killed and then created.
  4. The app should be able to stopped and then restarted.



## Features

These test features of the client sdk

### Test Feature 1.

The app must be able to make a tunnel and it should be shown that a resource can be reached through the tunnel.

Scope: library:connectedAndroidTest

### Test Feature 2.

The app must be able to get logs from the nabto-client-sdk.

Scope: library:connectedAndroidTest

### Test Feature 4.

The app must be able to make a stream.

Scope: clibrary:onnectedAndroidTest

### Test Feature 5.

The app must be able to make a coap request.

Scope: Scope: library:connectedAndroidTest

### Test Feature 6.

The app must be able to get the version of the nabto core library.
The app must be able to get the version of the nabto wrapper library.

## Connectivity

Test library:connectivity from an app with the nabto-client-sdk.

### Test Conn 1.

The app must be able to connect to a remote nabto-device.

Scope: library:connectedAndroidTest


### Test Conn 3.

The app must be able to connect to a local nabto-device on an ipv4 only local network.

Scope: manual-tests:connectedAndroidTest on a real device with given network

Make sure the test device and phone is on the same network.

run a test device: `./start_local_test_device.sh`

./gradlew :manual-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nabto.edge.client.test.ConnectionTest#connectLocal

### Test Conn 4.

The app must be able to connect to a local nabto-device on an ipv6 only local network.

Scope: :manual-tests:connectedAndroidTest on a real device with given network

join ipv6only network on both laptop and phone. Currently our embedded sdk test
devices has a problem with ipv6 and mdns so this probably fails.


### Test Conn 5.

On android, if there is no internet access on a local network, then
by default no traffic is sent to that network.

The app must be able to connect to a local nabto-device on a network without internet access on a android-device which has internet access via 3/4/5g

Scope: :manual-tests:connectedAndroidTest on a real device with given network

prerequisite: embedded sdk device and android joins an ipv4 network without internet access

run a test device: `./start_local_test_device.sh`

This is TODO since gradle does not seem to function without internet.

./gradlew :manual-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nabto.edge.client.test.ConnectionTest#connectLocal


### Test Conn 6.

The client can scan for mdns devices, subtypes and get txt records.

`./start_local_test_device.sh`

./gradlew :manual-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nabto.edge.client.test.MdnsTest


# Release Procedure

1. Create a git tag
2. See that the build passes, this runs unit tests.
3. Download manual-tests-debug-androidTest.apk and library-debug-androidTest.apk from jenkins
4. run
```
adb install manual-tests-debug-androidTest.apk
adb install library-debug-androidTest.apk
./start_local_test_devices.sh (in a seperate shell)
adb shell am instrument -w com.nabto.edge.client.test/androidx.test.runner.AndroidJUnitRunner
adb shell am instrument -w com.nabto.edge.client.manualtests.test/androidx.test.runner.AndroidJUnitRunner
```
5. Bonus: test library-ktx, iam-util and iam-util-ktx with connectedAndroidTests as described somewhere in this document.
6. Bonus: test the staging library from sonatype nexus in an example app, such that the version etc is validated.
7. when tests is passed close the staging repository on sonatype nexus, it takes some time, afterwards promote the staging reporisoty to a release.
8. Bonus: test that the artifacts on maven central works.
