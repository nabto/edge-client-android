#!/bin/bash

set -e

DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SRC_DIR=${DIR}/..

${DIR}/build_container.sh android_build_container ${SRC_DIR}/build-container

# Make sure build-cache exists such that docker does not create it as the root user.
mkdir -p $HOME/build-cache

# We cant use -t when running in jenkins
if [ -t 0 ]; then
    export USE_TTY="-t"
fi

docker run --rm -i ${USE_TTY} \
    --volume="${SRC_DIR}:/sandbox" \
    --volume=${HOME}/build-cache:/build-cache \
    --workdir="/sandbox" \
    -e ORG_GRADLE_PROJECT_GPG_SIGNING_KEY_BASE64 -e ORG_GRADLE_PROJECT_GPG_SIGNING_PASSWORD -e ORG_GRADLE_PROJECT_OSSRH_USERNAME -e ORG_GRADLE_PROJECT_OSSRH_PASSWORD \
    android_build_container build-scripts/android.sh "$@"

    # android_build_container echo "$@"

# android_build_container build-scripts/android.sh "$@"
