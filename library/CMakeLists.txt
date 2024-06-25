cmake_minimum_required(VERSION 3.10)
project(nabto_client_build)

include(ExternalProject)

message(INFO "running cmake")

set(nabto_client_sdk_dir ${CMAKE_CURRENT_SOURCE_DIR}/../nabto-client-sdk)

if (ANDROID_ABI MATCHES "arm64-v8a")
  set(VCPKG_TARGET_TRIPLET "arm64-android")
elseif(ANDROID_ABI MATCHES "armeabi-v7a")
  set(VCPKG_TARGET_TRIPLET "arm-neon-android")
elseif(ANDROID_ABI MATCHES "x86_64")
  set(VCPKG_TARGET_TRIPLET "x64-android")
elseif(ANDROID_ABI MATCHES "x86")
  set(VCPKG_TARGET_TRIPLET "x86-android")
else()
  message(FATAL_ERROR "unknown VCPKG_TARGET_TRIPLET for ${ANDROID_ABI}")
endif()

set(INSTALL_DIR ${CMAKE_CURRENT_BINARY_DIR}/andoid-${ANDROID_ABI})

ExternalProject_Add(nabto_client
    SOURCE_DIR ${nabto_client_sdk_dir}
    CMAKE_ARGS -GNinja -DCLIENT_BUILD_EXAMPLES=OFF -DCLIENT_BUILD_TESTS=OFF -DCLIENT_BUILD_ANDROID_JNI=ON -DANDROID_PLATFORM=${ANDROID_PLATFORM} -DVCPKG_CHAINLOAD_TOOLCHAIN_FILE=${ANDROID_NDK}/build/cmake/android.toolchain.cmake -DANDROID_ABI=${ANDROID_ABI} -DVCPKG_TARGET_TRIPLET=${VCPKG_TARGET_TRIPLET} -DCMAKE_BUILD_TYPE=RelWithDebInfo -DCMAKE_INSTALL_PREFIX=${INSTALL_DIR} ${SRC_DIR}
    INSTALL_COMMAND ninja install
)

INSTALL(DIRECTORY ${INSTALL_DIR}/java/com/nabto/edge/client/swig
        DESTINATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/java/com/nabto/edge/client/)

INSTALL(FILES ${INSTALL_DIR}/lib/libnabto_client_jni.so
        DESTINATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
