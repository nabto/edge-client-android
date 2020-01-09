package com.nabto.edge.client;

import android.net.Network;
import java.math.BigInteger;

import android.content.Context;

import com.nabto.edge.client.impl.NabtoClientImpl;

public abstract class NabtoClient {

    /**
     * Create a new instance of a nabto client.
     *
     * @param context  androids context e.g. an Application object.
     */
    public static NabtoClient create(Context context) {
        return new NabtoClientImpl(context);
    }

    /**
     * Set the log level, the log is logged to the standard android
     * log.
     *
     * This will maybe be removed in the future and replaced with a
     * more android way of defining the log level.
     *
     * @param level, the level to log upto. levels is error, warn,
     * info, trace. A level of trace includes all the less verbose
     * levels.
     */
    public abstract void setLogLevel(String level);

    /**
     * Create a new private key to use in a client.
     *
     * The normal scenario is that a private key is created once. It
     * is then saved in the client and reused later.
     */
    public abstract String createPrivateKey();

    /**
     * Create a connection. A connection is between this client and a
     * device.
     */
    public abstract Connection createConnection();
}
