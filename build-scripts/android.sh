#!/bin/bash

set -e

DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SRC_DIR=${DIR}/..

ANDROID_LIBRARY_JNILIBS_DIR=${SRC_DIR}/library/src/main/jniLibs
ANDROID_LIBRARY_JAVA_FOLDER=${SRC_DIR}/library/src/main/java/com/nabto/edge/client


function build_android_libraries {
    cd ${SRC_DIR}
    ./gradlew :library:build --rerun-tasks
    ./gradlew :library-ktx:build --rerun-tasks
    ./gradlew :iam-util:build --rerun-tasks
    ./gradlew :iam-util-ktx:build --rerun-tasks

    ./gradlew :library:assembleAndroidTest --rerun-tasks
    ./gradlew :library-ktx:assembleAndroidTest --rerun-tasks
    ./gradlew :iam-util:assembleAndroidTest --rerun-tasks
    ./gradlew :iam-util-ktx:assembleAndroidTest --rerun-tasks
    ./gradlew :manual-tests:assembleAndroidTest --rerun-tasks
}

function build_native_libraries {
    # build native libraries

    remove_existing_java_and_jni_files

    ${SRC_DIR}/nabto-client-sdk/build-scripts/android.sh build_arm64-v8a
    ${SRC_DIR}/nabto-client-sdk/build-scripts/android.sh build_armeabi-v7a
    ${SRC_DIR}/nabto-client-sdk/build-scripts/android.sh build_x86_64
    ${SRC_DIR}/nabto-client-sdk/build-scripts/android.sh build_x86
    copy_native_libs_to_android_library
}

function build_native_libraries_dev {
    # Only build for the two most used simulators and devices

    remove_existing_java_and_jni_files

    ${SRC_DIR}/nabto-client-sdk/build-scripts/android.sh build_arm64-v8a
    ${SRC_DIR}/nabto-client-sdk/build-scripts/android.sh build_x86_64
    copy_native_libs_to_android_library
}

function remove_existing_java_and_jni_files {
    # remove existing jni and java files such that we do not falsely believe
    # that a native library build succeded.
    ABIS="arm64-v8a armeabi-v7a x86 x86_64"
    for A in ${ABIS}; do
        rm -f ${ANDROID_LIBRARY_JNILIBS_DIR}/${ANDROID_ABI}/libnabto_client_jni.so
    done;

    rm -rf ${ANDROID_LIBRARY_JAVA_FOLDER}/swig
}

function copy_native_libs_to_android_library {
    # Copy libnabto_client_jni.so files to library/src/main/jniLibs/<ANDROID_ABI>/libnabto_client_jni.so

    JNILIBS_DIR=${SRC_DIR}/library/src/main/jniLibs

    #find ${JNILIBS_DIR} -iname "libnabto_client_jni.so" | xargs rm

    ABIS="arm64-v8a armeabi-v7a x86 x86_64"
    for ANDROID_ABI in ${ABIS}; do
        ABI_ARTIFACTS=${SRC_DIR}/nabto-client-sdk/artifacts/android-${ANDROID_ABI}
        OUTPUT_DIR=${JNILIBS_DIR}/${ANDROID_ABI}

        JNI_LIB_FILE=${ABI_ARTIFACTS}/lib/libnabto_client_jni.so
        #JNI_LIB_OUTPUT_FILE=${OUTPUT_DIR}/libnabto_client_jni.so
        if [ -f ${JNI_LIB_FILE} ]; then
            mkdir -p ${OUTPUT_DIR}
            echo "Copying ${JNI_LIB_FILE} to ${OUTPUT_DIR}"
            cp ${JNI_LIB_FILE} ${OUTPUT_DIR}
        else
            echo "${JNI_LIB_FILE} not found, skipping architecture ${ANDROID_ABI}"
        fi
    done;

    # Copy the generated java files from one of the android architecture builds

    JAVA_FOLDER=${SRC_DIR}/library/src/main/java/com/nabto/edge/client
    mkdir -p ${JAVA_FOLDER}

    JNI_JAVA_WRAPPER_SOURCE_DIR=${SRC_DIR}/nabto-client-sdk/artifacts/android-arm64-v8a/java/com/nabto/edge/client/swig
    if [ -d ${JNI_JAVA_WRAPPER_SOURCE_DIR} ]; then
        echo "Copying JNI Java wrapper source from ${JNI_JAVA_WRAPPER_SOURCE_DIR} to ${JAVA_FOLDER}"
        cp -r ${JNI_JAVA_WRAPPER_SOURCE_DIR} ${JAVA_FOLDER}
    else
        echo "Cannot find JNI Java wrapper source"
    fi
}



function deploy {
    cd ${SRC_DIR}
    ./gradlew publish
}

function run_unit_tests {
    ./gradlew test
}

function all {
    build_native_libraries
    build_android_libraries
    run_unit_tests
    echo "finding built apks (jenkins help)"
    find ${SRC_DIR} -iname "*.apk"
    echo "finding built aars (jenkins help)"
    find ${SRC_DIR} -iname "*.aar"
}

function help {
    echo "unknown command $0 $@"
}

function test {
    echo "Testing library against a set of standard devices"
    ./gradlew library:phonesDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notAnnotation=com.nabto.edge.client.ComplexTest
}

case $1 in
    "")
        all
        ;;
    "build_android_libraries")
        build_android_libraries
        ;;
    "deploy")
        deploy
        ;;
    "test")
        test
        ;;
    "build_native_libraries_dev")
        build_native_libraries_dev
        ;;
    "build_native_libraries")
        build_native_libraries
        ;;
    *)
        help $@
        ;;
esac
