package com.nabto.edge.client;

public abstract class ConnectionEventsCallback {
    public static int CONNECTED = com.nabto.client.jni.ConnectionEventsCallback.CONNECTED();

    public static int CLOSED = com.nabto.client.jni.ConnectionEventsCallback.CLOSED();
    public static int CHANNEL_CHANGED = com.nabto.client.jni.ConnectionEventsCallback.CHANNEL_CHANGED();
    public abstract void onEvent(int event);
}
