FROM ubuntu:18.04

USER root

RUN apt-get update && apt-get install curl unzip git build-essential openjdk-8-jdk swig -y

ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip" \
    ANDROID_HOME="/usr/local/android-sdk" \
    ANDROID_VERSION=19 \
    ANDROID_BUILD_TOOLS_VERSION=29.0.2
ENV ANDROID_SDKMANAGER=$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager
# Download Android SDK
RUN mkdir "$ANDROID_HOME" .android \
    && cd "$ANDROID_HOME" \
    && curl -s -o sdk.zip $SDK_URL \
    && unzip -q sdk.zip \
    && rm sdk.zip

#wtf
RUN mv $ANDROID_HOME/cmdline-tools $ANDROID_HOME/foo
RUN mkdir $ANDROID_HOME/cmdline-tools
RUN mv $ANDROID_HOME/foo $ANDROID_HOME/cmdline-tools/latest

RUN yes | $ANDROID_SDKMANAGER --licenses

# Install Android Build Tool and Libraries
RUN $ANDROID_SDKMANAGER "tools" "platform-tools"
RUN $ANDROID_SDKMANAGER "build-tools;29.0.2"
RUN $ANDROID_SDKMANAGER "platforms;android-29"
RUN $ANDROID_SDKMANAGER "ndk-bundle"
RUN $ANDROID_SDKMANAGER "cmake;3.18.1"

RUN yes | $ANDROID_SDKMANAGER --licenses
