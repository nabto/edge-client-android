#!/bin/bash

set -e

adb logcat -c

./gradlew library:connectedAndroidTest
./gradlew library-ktx:connectedAndroidTest
./gradlew iam-util:connectedAndroidTest
./gradlew iam-util-ktx:connectedAndroidTest
