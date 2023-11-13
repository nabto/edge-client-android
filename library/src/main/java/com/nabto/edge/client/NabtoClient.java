package com.nabto.edge.client;

import android.net.Network;
import java.math.BigInteger;

import android.content.Context;

import com.nabto.edge.client.impl.NabtoClientImpl;

/**
 * This class is the Nabto Edge Client SDK main entry point.
 *
 * It allows you to create private keys to use to open a connection. And to create the actual
 * connection object used to start interaction with a Nabto Edge embedded device. The Client object
 * must be kept alive for the duration of all connections created from it.
 */
public abstract class NabtoClient implements AutoCloseable {

    /**
     * Create a new instance of a nabto client.
     *
     * @param context Androids context e.g. an Application object.
     * @return The NabtoClient instance created.
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
     * @param level The level to log upto. levels is error, warn,
     * info, trace. A level of trace includes all the less verbose
     * levels.
     */
    public abstract void setLogLevel(String level);

    /**
     * Create a new private key to use in a client.
     *
     * The normal scenario is that a private key is created once. It
     * is then saved in the client and reused later.
     *
     * @return The created private key.
     */
    public abstract String createPrivateKey();

    /**
     * Create a connection. A connection is between this client and a
     * device. Returned object must be kept alive while in use.
     *
     * @return The created connection.
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
     * @deprecated
     * This method is deprecated in favor of using createMdnsScanner()
     *
     * Listen for mdns results.
     *
     * @param listener The mdns result listener to add.
     */
    @Deprecated
    public abstract void addMdnsResultListener(MdnsResultListener listener);

    /**
     * DEPRECATED: Add a mdns result listener which only returns results for the given subtype.
     * @deprecated
     * This method is deprecated in favor of using createMdnsScanner()
     *
     * @param listener The mdns result listener to add.
     * @param subtype The subtype to listen for.
     */
    @Deprecated
    public abstract void addMdnsResultListener(MdnsResultListener listener, String subtype);

    /**
     * Remove a mdns result listener.
     * @deprecated
     * This method is deprecated in favor of using createMdnsScanner()
     *
     * @param listener The mdns result listener to remove.
     */
    @Deprecated
    public abstract void removeMdnsResultListener(MdnsResultListener listener);

    /**
     * Get the version of the underlying native nabto client library.
     *
     * @return String representation of the SDK version.
     */
    public abstract String version();

    /**
     * Releases any resources associated with the Client instance. This method is
     * called automatically at the end of a try-with-resources block, which
     * helps to ensure that resources are released promptly and reliably.
     *
     * <p>Example of using a Client object within a try-with-resources statement:</p>
     * <pre>
     * try (NabtoClient client = NabtoClient.create(...)) {
     *     // ... use client
     * }
     * </pre>
     *
     * <p>With this setup, {@code close()} will be called automatically on
     * {@code client} at the end of the block, releasing any underlying
     * native Nabto Client SDK resources without any further action required on the application.
     *
     * <p></p>If the try-with-resources construct is not feasible, the application must manually call close()
     * when the NabtoClient instance is no longer needed.</p>
     *
     * <p>Unlike the {@link AutoCloseable#close()} method, this {@code close()}
     * method does not throw any exceptions.</p>
     */
    @Override
    public abstract void close();
}
