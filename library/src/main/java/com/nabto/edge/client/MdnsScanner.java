package com.nabto.edge.client;

/**
 * This class scans for local mDNS enabled devices.
 * You can create an MdnsScanner by calling NabtoClient.createMdnsScanner()
 */

public interface MdnsScanner {
    void start();

    void stop();

    boolean isStarted();

    void addMdnsResultListener(MdnsResultListener listener);

    void removeMdnsResultListener(MdnsResultListener listener);
}