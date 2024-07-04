# Nabto client sdk for Android

This repository contains the Java and Kotlin wrapper libraries for the Nabto
Edge Client SDK and integration code for the Nabto IAM system.

The folder library contains a wrapper for the Nabto Edge Client SDK library.

## Android Nabto Edge Client SDK Library

This library consists of a Java Wrapper and JNI code for the native Nabto Edge
Client SDK library.

## Release strategy

All non tagged builds are put into the snapshot repository at  https://s01.oss.sonatype.org/content/repositories/snapshots/com/nabto/edge/client/library/ using a naming strategy which is "<branch-name>-SNAPSHOT".

All tagged releases are going to the staging repository where they need to be accepted before they can go to the maven central repository. Only visible if logged into sonatype.org (upper right corner).

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



## Publishing the library to the local maven repository

run `./gradlew publishToMavenLocal`

## Publish to maven OSSRH.

This requires OSSRH_USERNAME, OSSRH_PASSWORD, GPG_SIGNING_PASSWORD and GPG_SIGNING_KEY_BASE64 to be set. These variables can be set in ~/.gradle/gradle.properties for local builds or via the environment for CI builds. ORG_GRADLE_PROJECT_OSSRH_USERNAME etc. Credentials can be found in dokuwiki.

The base64 key can be created by `gpg --armor --export-secret-keys keyid | base64 -w 0`

`./gradlew publish`

Tagged releases will be sent to the staging repository at sonatype https://s01.oss.sonatype.org/ Where they manually needs to be promoted as releases before they can be found in maven central (see below).

Non release builds aka snapshots can be found at https://s01.oss.sonatype.org/content/repositories/snapshots/com/nabto/edge/client/library/

### Using a build in a Sonatype Staging Repository

To use a staging build, modify the `dependencyResolutionManagement` section of settings.gradle to include the staging repository (the id `10XX` is incremented with each uploaded build (and staging builds may only removed after a short while)):

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url "https://s01.oss.sonatype.org/service/local/repositories/comnabto-10XX/content/"
        }
        google()
        mavenCentral()
    }
}
```


### Promoting a build through Sonatype

The process is described on [sonatype.org](https://central.sonatype.org/publish/release/#locate-and-examine-your-staging-repository) - basically you must _close_ the staging build before it can be released for public access:

> After your deployment the repository will be in an Open status. You can evaluate the deployed components in the repository using the Contents tab. If you believe everything is correct you, can press the Close button above the list. This will trigger the evaluations of the components against the requirements.
>
> Closing will fail if your components do not meet the requirements. If this happens, you can press Drop and the staging repository will be deleted. This allows you to correct any problems with the components and the deployment process and re-run the deployment. Details are available in the Activity tab below the list by selecting. Press on the individual steps for further details.
>
> Once you have successfully closed the staging repository, you can release it by pressing the Release button. This will move the components into the release repository of OSSRH where it will be synced to the Central Repository.


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
