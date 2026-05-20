package com.nabto.edge.client;

/**
 * This abstract class allows implementations to receive notification from the SDK on connection events.
 *
 * Applications registers for notifications using Connection.addConnectionEventsListener().
 *
 * The following events can be emitted:
 * - `CONNECTED`: a connection is established
 * - `CLOSED`: a connection is closed
 * - `CHANNEL_CHANGED`: the underlying channel has changed, e.g. from relay to p2p
 * - `WAITING_FOR_ATTACH`: emitted when connecting with the `IgnoreDeviceNotAttached`
 *   option set and the device is not currently attached; the client will keep waiting
 *   for the device to attach and then complete the connection.
 */
public abstract class ConnectionEventsCallback {
    public static int CONNECTED = com.nabto.edge.client.swig.ConnectionEventsCallback.CONNECTED();
    public static int CLOSED = com.nabto.edge.client.swig.ConnectionEventsCallback.CLOSED();
    public static int CHANNEL_CHANGED = com.nabto.edge.client.swig.ConnectionEventsCallback.CHANNEL_CHANGED();
    public static int WAITING_FOR_ATTACH = com.nabto.edge.client.swig.ConnectionEventsCallback.WAITING_FOR_ATTACH();

    /**
     * Invoked when an event occurs.
     *
     * The implementor of this function should not let exceptions escape into
     * the caller as the caller has no way to handle the exceptions
     * meaningfully.
     *
     * @param event the event which had happened.
     */
    public abstract void onEvent(int event);
}
