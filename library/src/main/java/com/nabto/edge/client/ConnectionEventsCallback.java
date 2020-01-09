package com.nabto.edge.client;

public abstract class ConnectionEventsCallback {
    public static int CONNECTED = com.nabto.edge.client.swig.ConnectionEventsCallback.CONNECTED();

    public static int CLOSED = com.nabto.edge.client.swig.ConnectionEventsCallback.CLOSED();
    public static int CHANNEL_CHANGED = com.nabto.edge.client.swig.ConnectionEventsCallback.CHANNEL_CHANGED();
    public abstract void onEvent(int event);
}
