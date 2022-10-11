package com.nabto.edge.client;

import android.net.Network;
import java.math.BigInteger;

import android.content.Context;

import com.nabto.edge.client.MdnsScanner;
import com.nabto.edge.client.impl.NabtoClientImpl;

/**
 * This class is the Nabto Edge Client SDK main entry point.
 *
 * It allows you to create private keys to use to open a connection. And to create the actual
 * connection object used to start interaction with a Nabto Edge embedded device. The Client object
 * must be kept alive for the duration of all connections created from it.
 */
public abstract class NabtoClient {

    /**
     * Create a new instance of a nabto client.
     *
     * @param context  androids context e.g. an Application object.
     * @return the NabtoClient instance created.
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
     * @return the created privatekey.
     */
    public abstract String createPrivateKey();

    /**
     * Create a connection. A connection is between this client and a
     * device. Returned object must be kept alive while in use.
     * @return the created connection.
     */
    public abstract Connection createConnection();

    /**
     * Create an MdnsScanner to scan for devices using mDNS.
     * Fundamentally just uses the NabtoClient.addMdnsResultListener method.
     */
    public abstract MdnsScanner createMdnsScanner();

    /**
     * Create an MdnsScanner to scan for devices using mDNS with a specific subtype.
     * Fundamentally just uses the NabtoClient.addMdnsResultListener method.
     *
     * @param subtype The subtype to scan for.
     */
    public abstract MdnsScanner createMdnsScanner(String subtype);

    /**
     * Add a listener for mdns results.
     *
     * Listen for mdns results.
     * @param listener the mdns result listener.
     */
    public abstract void addMdnsResultListener(MdnsResultListener listener);

    /**
     * Add a mdns result listener which only returns results for the given subtype.
     * @param listener the mdns result listener to add.
     * @param subtype The subtype to listen for.
     */
    public abstract void addMdnsResultListener(MdnsResultListener listener, String subtype);

    /**
     * Remove a mdns result listener.
     * @param listener the mdns result listener to remove.
     */
    public abstract void removeMdnsResultListener(MdnsResultListener listener);

    /**
     * Get the version of the underlying native nabto client library.
     *
     * @return version string
     */
    public abstract String version();
}
