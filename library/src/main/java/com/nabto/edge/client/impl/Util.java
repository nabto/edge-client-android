package com.nabto.edge.client.impl;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.nabto.edge.client.NabtoCallback;
import com.nabto.edge.client.swig.FutureBuffer;
import com.nabto.edge.client.swig.FutureCallback;

public class Util {

    public static void logUnhandledCallbackException(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        Log.e("nabto", "Callback into application from SDK threw an exception (will not be propagated to Nabto Client SDK as this will cause crash):  " + sw);
    }

    public static FutureCallback makeFutureCallback(NabtoCallback<Void> callback) {
        // Most of the future callbacks used just pass through to a NabtoCallback like this
        FutureCallback cb = new FutureCallback() {
            public void run(com.nabto.edge.client.swig.Status status) {
                Optional<Void> opt = Optional.empty();
                try {
                    callback.run(status.getErrorCode(), opt);
                } catch (Throwable t) {
                    logUnhandledCallbackException(t);
                } finally {
                    callbackReferences.remove(this);
                }
            }
        };
        callbackReferences.add(cb);
        return cb;
    }

    public static FutureCallback makeFutureCallbackBuffer(NabtoCallback<byte[]> callback, FutureBuffer buffer) {
        // Most of the future callbacks used just pass through to a NabtoCallback like this
        FutureCallback cb = new FutureCallback() {
            public void run(com.nabto.edge.client.swig.Status status) {
                Optional<Void> opt = Optional.empty();
                try {
                    callback.run(status.getErrorCode(), Optional.of(buffer.getResult()));
                } catch (Throwable t) {
                    logUnhandledCallbackException(t);
                } finally {
                    callbackReferences.remove(this);
                }
            }
        };
        callbackReferences.add(cb);
        return cb;
    }
    private static final Set<FutureCallback> callbackReferences = ConcurrentHashMap.newKeySet();
}
