package com.nabto.edge.client.impl;

import android.net.wifi.WifiManager;

import java.util.Optional;
import com.nabto.edge.client.ErrorCodes;
import com.nabto.edge.client.NabtoEOFException;
import com.nabto.edge.client.NabtoCallback;
import com.nabto.edge.client.swig.FutureBuffer;

public class StreamImpl implements com.nabto.edge.client.Stream {

    com.nabto.edge.client.swig.Stream stream;
    private final CleanerService.Cleanable cleanable;

    StreamImpl(com.nabto.edge.client.swig.Stream stream) {
        this.stream = stream;
        this.cleanable = createAndRegisterCleanable(this, stream);
    }

    public void open(int streamPort) {
        try {
            stream.open(streamPort).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void openCallback(int streamPort, NabtoCallback callback) {
        try {
            stream.open(streamPort).callback(Util.makeFutureCallback(callback));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public byte[] readSome() throws NabtoEOFException {
        try {
            return stream.readSome(1024).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            if (e.status().getErrorCode() == com.nabto.edge.client.swig.Status.getEND_OF_FILE()) {
                throw new NabtoEOFException(e);
            } else {
                throw new com.nabto.edge.client.NabtoRuntimeException(e);
            }
        }
    }

    public void readSomeCallback(NabtoCallback<byte[]> callback) throws NabtoEOFException {
        try {
            FutureBuffer buffer = stream.readSome(1024);
            buffer.callback(Util.makeFutureCallbackBuffer(callback, buffer));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public byte[] readAll(int length) throws NabtoEOFException {
        try {
            return stream.readAll(length).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            if (e.status().getErrorCode() == com.nabto.edge.client.swig.Status.getEND_OF_FILE()) {
                throw new NabtoEOFException(e);
            } else {
                throw new com.nabto.edge.client.NabtoRuntimeException(e);
            }
        }
    }

    public void readAllCallback(int length, NabtoCallback<byte[]> callback) {
        try {
            FutureBuffer buffer = stream.readAll(length);
            buffer.callback(Util.makeFutureCallbackBuffer(callback, buffer));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void write(byte[] bytes) {
        try {
            stream.write(bytes).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void writeCallback(byte[] bytes, NabtoCallback callback) {
        stream.write(bytes).callback(Util.makeFutureCallback(callback));
    }

    @Override
    public void streamClose() {
        try {
            stream.close().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    // @TODO: Perhaps we dont need a callback for this function? Like abort below, it could just be run without blocking or setting a callback
    public void streamCloseCallback(NabtoCallback callback) {
        try {
            stream.close().callback(Util.makeFutureCallback(callback));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void abort() {
        stream.abort();
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    /// static helper to ensure no "this" is captured accidentally
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, com.nabto.edge.client.swig.Stream nativeHandle) {
        return CleanerService.instance().register(o, () -> nativeHandle.delete());
    }
}
