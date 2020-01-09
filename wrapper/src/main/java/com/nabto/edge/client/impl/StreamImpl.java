package com.nabto.edge.client.impl;

public class StreamImpl implements com.nabto.edge.client.Stream {

    com.nabto.edge.client.swig.Stream stream;

    StreamImpl(com.nabto.edge.client.swig.Stream stream) {
        this.stream = stream;
    }

    public void open(int streamPort) {
        try {
            stream.open(streamPort).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public byte[] readSome() {
        try {
            return stream.readSome(1024).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public byte[] readAll(int length) {
        try {
            return stream.readAll(length).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public void write(byte[] bytes) {
        try {
            stream.write(bytes).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public void close() {
        try {
            stream.close().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public void abort() {
        stream.abort();
    }

}
