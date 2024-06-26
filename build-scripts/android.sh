#!/bin/bash

set -e

DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SRC_DIR=${DIR}/..

if [ "$ANDROID_NDK_HOME" == "" ]; then
    echo "missing environment variable ANDROID_NDK_HOME this is needed by vcpkg even that the correct variable name is ANDROID_NDK"
    exit 1
fi

function build {
    cd ${SRC_DIR}
    ./gradlew :library:build --rerun-tasks
    ./gradlew :library-ktx:build --rerun-tasks
    ./gradlew :iam-util:build --rerun-tasks
    ./gradlew :iam-util-ktx:build --rerun-tasks
}

function deploy {
    cd ${SRC_DIR}
    ./gradlew publish
}

function help {
    echo "read the script"
}

case $1 in
    "")
        build
        ;;
    "build")
        build
        ;;
    "deploy")
        deploy
        ;;
    *)
        help
        ;;
esac
