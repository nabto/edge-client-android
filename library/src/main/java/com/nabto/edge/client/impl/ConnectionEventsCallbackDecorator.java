package com.nabto.edge.client.impl;

import com.nabto.edge.client.ConnectionEventsCallback;

public class ConnectionEventsCallbackDecorator extends com.nabto.edge.client.swig.ConnectionEventsCallback {
    private ConnectionEventsCallback callback;
    ConnectionEventsCallbackDecorator(ConnectionEventsCallback cb)
    {
        callback = cb;
    }

    @Override
    public void onEvent(int event) {
        try {
            callback.onEvent(event);
        } catch(Throwable t) {
            Util.logUnhandledCallbackException(t);
        }
    }
}
