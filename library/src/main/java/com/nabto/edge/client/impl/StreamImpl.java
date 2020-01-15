package com.nabto.edge.client.impl;

import com.nabto.edge.client.NabtoEOFException;

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

    public byte[] readSome() throws NabtoEOFException {
        try {
            return stream.readSome(1024).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            if (e.status().getErrorCode() == com.nabto.edge.client.swig.Status.getEND_OF_FILE()) {
                throw new NabtoEOFException();
            } else {
                throw new com.nabto.edge.client.NabtoException(e);
            }
        }
    }

    public byte[] readAll(int length) throws NabtoEOFException {
        try {
            return stream.readAll(length).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            if (e.status().getErrorCode() == com.nabto.edge.client.swig.Status.getEND_OF_FILE()) {
                throw new NabtoEOFException();
            } else {
                throw new com.nabto.edge.client.NabtoException(e);
            }
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
