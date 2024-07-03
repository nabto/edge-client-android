FROM ubuntu:22.04

USER root

RUN apt-get update && apt-get install curl unzip zip git build-essential openjdk-17-jdk swig cmake zip pkg-config ninja-build ccache tar wget python3 -y

ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
    ANDROID_HOME="/usr/local/android-sdk"
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

ARG NDK_VERSION=26.3.11579264

# Install Android Build Tool and Libraries
RUN $ANDROID_SDKMANAGER "tools" "platform-tools"
RUN $ANDROID_SDKMANAGER "build-tools;34.0.0"
RUN $ANDROID_SDKMANAGER "platforms;android-34"
RUN $ANDROID_SDKMANAGER "ndk-bundle"
#RUN $ANDROID_SDKMANAGER "cmake;3.18.1"
RUN $ANDROID_SDKMANAGER "ndk;$NDK_VERSION"

RUN yes | $ANDROID_SDKMANAGER --licenses

ARG DOCKER_USER=default_user
ARG DOCKER_USER_UID=1000
ARG DOCKER_USER_GID=1000
RUN groupadd -g ${DOCKER_USER_GID} ${DOCKER_USER} && adduser --uid ${DOCKER_USER_UID} --gid ${DOCKER_USER_GID} --gecos "" --disabled-password ${DOCKER_USER}
USER $DOCKER_USER

ENV VCPKG_BINARY_SOURCES=clear;files,/build-cache/edge-client-android/vcpkg,readwrite
ENV CCACHE_DIR=/build-cache/edge-client-android/ccache
ENV CMAKE_CXX_COMPILER_LAUNCHER=ccache
ENV ANDROID_NDK_HOME=/usr/local/android-sdk/ndk/${NDK_VERSION}

WORKDIR /sandbox
