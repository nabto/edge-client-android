#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

rm -f $DIR/src/main/java/com/nabto/edge/client/swig/*.java
rm -f $DIR/src/swig/*.hpp
rm -f $DIR/src/swig/*.cpp

mkdir -p $DIR/src/swig

swig -c++ -java -package com.nabto.edge.client.swig -outdir $DIR/src/main/java/com/nabto/edge/client/swig -o $DIR/src/swig/nabto_client_wrap.cpp -D__ANDROID__ $DIR/../nabto-client-sdk/cpp_wrapper/nabto_client.i
