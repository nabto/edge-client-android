cmake_minimum_required(VERSION 3.10)

set(libressltar ${CMAKE_CURRENT_BINARY_DIR}/libressl.tar.gz)
set(libressldir ${CMAKE_CURRENT_BINARY_DIR}/libressl-2.9.1)

if(NOT EXISTS ${libressldir})
  if (NOT EXISTS ${libressltar})
    file(DOWNLOAD https://downloads.nabto.com/assets/3rdparty-source/libressl/libressl-2.9.1.tar.gz ${libressltar}
      TIMEOUT 60
      EXPECTED_HASH SHA256=39e4dd856694dc10d564201e4549c46d2431601a2b10f3422507e24ccc8f62f8
      TLS_VERIFY ON
      )
  endif()
  execute_process(COMMAND ${CMAKE_COMMAND} -E tar -xf ${libressltar}
                  WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR})
endif()

set(LIBRESSL_APPS OFF CACHE BOOL "")
set(LIBRESSL_TESTS OFF CACHE BOOL "")

add_subdirectory(${libressldir} ${CMAKE_CURRENT_BINARY_DIR}/libressl-2.9.1)

set(NABTO_STATIC_OPENSSL true)
set(NABTO_STATIC_OPENSSL_INCLUDE_DIR ${CMAKE_CURRENT_BINARY_DIR}/libressl/include)
set(NABTO_STATIC_OPENSSL_SSL_LIBARY ssl)
set(NABTO_STATIC_OPENSSL_CRYPTO_LIBRARY crypto)

message(info ${NABTO_STATIC_OPENSSL_SSL_LIBARY})

find_library(log-lib log)

set(CLIENT_BUILD_EXAMPLES OFF CACHE BOOL "")
set(CLIENT_BUILD_TESTS OFF CACHE BOOL "")

add_subdirectory(../nabto-client-sdk ${CMAKE_BINARY_DIR}/nabto_client)

add_custom_command(WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  COMMAND bash "generate_swig.sh"
  DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/../nabto-client-sdk/cpp_wrapper/nabto_client.i"
  OUTPUT "${CMAKE_CURRENT_SOURCE_DIR}/src/jni/nabto_client_wrap.cpp"
  )


set(src
  ${CMAKE_CURRENT_SOURCE_DIR}/../nabto-client-sdk/cpp_wrapper/nabto_client_impl.cpp
  ${CMAKE_CURRENT_SOURCE_DIR}/../nabto-client-sdk/cpp_wrapper/nabto_client.cpp
  ${CMAKE_CURRENT_SOURCE_DIR}/src/swig/nabto_client_wrap.cpp
  )

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../nabto-client-sdk/cpp_wrapper)
add_library(nabto_client_jni SHARED ${src})
target_link_libraries(nabto_client_jni nabto_client ${log-lib})
