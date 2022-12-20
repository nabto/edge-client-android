package com.nabto.edge.client;

/**
 * An MdnsScanner scans for local mDNS enabled devices.
 */
public interface MdnsScanner {
    /**
     * Start the scan for local devices using mDNS.
     */
    public void start();

    /**
     * Stop an active scan.
     */
    public void stop();

    /**
     * Query if a scan is active.
     */
    public boolean isStarted();

    /**
     * Add an mDNS result listener, invoked when an mDNS result is retrieved. Scan must be started separately (with start()).
     * @param receiver An implementation of the MdnsResultListener interface
     */
    public void addMdnsResultReceiver(MdnsResultListener receiver);

    /**
     * Remove an mDNS result listener.
     * @param receiver An implementation of the MdnsResultListener interface
     */
    public void removeMdnsResultReceiver(MdnsResultListener receiver);
}
