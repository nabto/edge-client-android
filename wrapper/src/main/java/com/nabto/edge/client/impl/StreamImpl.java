package com.nabto.edge.client.impl;

import com.nabto.edge.client.Stream;

public class StreamImpl implements com.nabto.edge.client.Stream {

    com.nabto.client.jni.Stream stream;

    StreamImpl(com.nabto.client.jni.Stream stream) {
        this.stream = stream;
    }

    public void open(int streamPort) {
        try {
            stream.open(streamPort).waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public byte[] readSome() {
        try {
            return stream.readSome(1024).waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public byte[] readAll(int length) {
        try {
            return stream.readAll(length).waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public void write(byte[] bytes) {
        try {
            stream.write(bytes).waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public void close() {
        try {
            stream.close().waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public void abort() {
        stream.abort();
    }

}
