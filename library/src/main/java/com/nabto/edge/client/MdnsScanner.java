package com.nabto.edge.client;

/**
 * This class scans for local mDNS enabled devices.
 * You can create an MdnsScanner by calling NabtoClient.createMdnsScanner()
 */
public interface MdnsScanner {
    /**
     * Start the scan for local devices using mDNS.
     *
     * Add result listeners prior to invoking to ensure all results are retrieved.
     */
    void start();


    /**
     * Stop an active scan.
     */
    void stop();

    /**
     * Query if a scan is active.
     */
    boolean isStarted();

    /**
     * Add an mDNS result callback, invoked when an mDNS result is retrieved. Scan must be started separately (with start()).
     * @param cb An implementation of the MdnsResultListener interface
     */
    void addMdnsResultListener(MdnsResultListener listener);

    /**
     * Remove an mDNS result callback.
     * @param cb An implementation of the MdnsResultListener interface
     */
    void removeMdnsResultListener(MdnsResultListener listener);
}