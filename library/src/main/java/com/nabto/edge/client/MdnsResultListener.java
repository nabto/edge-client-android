package com.nabto.edge.client;

/**
 * This interface specifies a callback function to receive mDNS results.
 */
public interface MdnsResultListener {

    /**
     * The implementation is invoked when an mDNS result is ready.
     *
     * The implementer of the function should not let exceptions escape into the
     * caller as the caller has no meaningful way to handle the exceptions.
     *
     * @param result The callback result.
     */
    public void onChange(MdnsResult result);
}
