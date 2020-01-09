package com.nabto.edge.client.impl;

import com.nabto.edge.client.ConnectionEventsCallback;

public class ConnectionEventsCallbackDecorator extends com.nabto.client.jni.ConnectionEventsCallback {
    private ConnectionEventsCallback callback;
    ConnectionEventsCallbackDecorator(ConnectionEventsCallback cb)
    {
        callback = cb;
    }

    @Override
    public void onEvent(int event) {
        super.onEvent(event);
        callback.onEvent(event);
    }
}
