package com.nabto.edge.client.impl;

import java.util.Optional;
import com.nabto.edge.client.NabtoCallback;
import com.nabto.edge.client.swig.FutureCallback;

public class Util {
    public static FutureCallback makeFutureCallback(NabtoCallback<Void> callback) {
        // Most of the future callbacks used just pass through to a NabtoCallback like this
        FutureCallback cb = new FutureCallback() {
            public void run(com.nabto.edge.client.swig.Status status) {
                Optional<Void> opt = Optional.empty();
                callback.run(status.getErrorCode(), opt);
            }
        };
        return cb;
    }
}