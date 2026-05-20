FROM ubuntu:22.04

USER root

RUN DEBIAN_FRONTEND=noninteractive apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install curl unzip zip git build-essential openjdk-17-jdk swig cmake zip pkg-config ninja-build ccache tar wget python3 awscli -y

ARG DOCKER_USER=defaultuser
ARG DOCKER_USER_UID=1000
ARG DOCKER_USER_GID=1000
RUN groupadd -g ${DOCKER_USER_GID} ${DOCKER_USER} \
    && adduser --uid ${DOCKER_USER_UID} --gid ${DOCKER_USER_GID} --gecos "" --disabled-password ${DOCKER_USER} \
    && chown -R ${DOCKER_USER}:${DOCKER_USER} /home/${DOCKER_USER}
USER $DOCKER_USER

WORKDIR /home/${DOCKER_USER}

ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip" \
    ANDROID_HOME="/home/defaultuser/Android/Sdk"
ENV ANDROID_SDKMANAGER=$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager
ENV ANDROID_SDK_ROOT=$ANDROID_HOME

# Download and unzip Android SDK
RUN mkdir -p "$ANDROID_HOME" .android \
    && cd "$ANDROID_HOME" \
    && curl -s -o sdk.zip $SDK_URL \
    && unzip -q sdk.zip \
    && rm sdk.zip

# sdkmanager expects to be run from $ANDROID_HOME/cmdline-tools/{version}/bin/sdkmanager otherwise it will error
RUN mv $ANDROID_HOME/cmdline-tools $ANDROID_HOME/Temp
RUN mkdir $ANDROID_HOME/cmdline-tools
RUN mv $ANDROID_HOME/Temp $ANDROID_HOME/cmdline-tools/latest

RUN yes | $ANDROID_SDKMANAGER --licenses

ARG NDK_VERSION=28.2.13676358

# Install Android Build Tool and Libraries
RUN $ANDROID_SDKMANAGER "tools" "platform-tools"
RUN $ANDROID_SDKMANAGER "build-tools;36.0.0"
RUN $ANDROID_SDKMANAGER "platforms;android-36"
RUN $ANDROID_SDKMANAGER "ndk-bundle"
RUN $ANDROID_SDKMANAGER "ndk;$NDK_VERSION"

RUN yes | $ANDROID_SDKMANAGER --licenses

ENV ANDROID_NDK_HOME=$ANDROID_HOME/ndk/${NDK_VERSION}
ENV VCPKG_BINARY_SOURCES=clear;files,/build-cache/edge-client-android/vcpkg,readwrite
ENV CCACHE_DIR=/build-cache/edge-client-android/ccache
ENV CMAKE_CXX_COMPILER_LAUNCHER=ccache

WORKDIR /sandbox
