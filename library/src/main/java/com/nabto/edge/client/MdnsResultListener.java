package com.nabto.edge.client;

/**
 * This interface specifies a callback function to receive mDNS results
 */
public interface MdnsResultListener {
    /*
     * The implementation is invoked when an mDNS result is ready
     * @param result The callback result.
     */
    public void onChange(MdnsResult result);
}
