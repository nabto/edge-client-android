FROM ubuntu:18.04

USER root

RUN apt-get update && apt-get install curl unzip git build-essential openjdk-8-jdk swig -y

ENV SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip" \
    ANDROID_HOME="/usr/local/android-sdk" \
    ANDROID_VERSION=19 \
    ANDROID_BUILD_TOOLS_VERSION=29.0.2
# Download Android SDK
RUN mkdir "$ANDROID_HOME" .android \
    && cd "$ANDROID_HOME" \
    && curl -s -o sdk.zip $SDK_URL \
    && unzip -q sdk.zip \
    && rm sdk.zip

RUN yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses

# Install Android Build Tool and Libraries
RUN $ANDROID_HOME/tools/bin/sdkmanager "tools" "platform-tools"
RUN $ANDROID_HOME/tools/bin/sdkmanager "build-tools;29.0.2"
RUN $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-29"
RUN $ANDROID_HOME/tools/bin/sdkmanager "ndk-bundle"
RUN $ANDROID_HOME/tools/bin/sdkmanager "cmake;3.18.1"

RUN yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses
