#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

rm -f $DIR/src/main/java/com/nabto/client/jni/*.java
rm -f $DIR/src/jni/*.hpp
rm -f $DIR/src/jni/*.cpp

mkdir -p $DIR/src/jni

swig -c++ -java -package com.nabto.client.jni -outdir $DIR/src/main/java/com/nabto/client/jni -o $DIR/src/jni/nabto_client_wrap.cpp -D__ANDROID__ $DIR/../../cpp_wrapper/nabto_client.i
