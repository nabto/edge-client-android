package com.nabto.edge.client;

/**
 * This abstract class allows implementations to receive notification from the SDK on connection events.
 *
 * Applications registers for notifications using Connection.addConnectionEventsListener().
 *
 * The following events are supported:
 *
 * ```
 * CONNECTED: a connection is established
 * CLOSED: a connection is closed
 * CHANNEL_CHANGED: the underlying channel has changed, e.g. from relay to p2p
 * ```
 */
public abstract class ConnectionEventsCallback {
    public static int CONNECTED = com.nabto.edge.client.swig.ConnectionEventsCallback.CONNECTED();
    public static int CLOSED = com.nabto.edge.client.swig.ConnectionEventsCallback.CLOSED();
    public static int CHANNEL_CHANGED = com.nabto.edge.client.swig.ConnectionEventsCallback.CHANNEL_CHANGED();

    /**
     * Invoked when an event occurs.
     * @param event the event which had happened.
     */
    public abstract void onEvent(int event);
}
