package com.nabto.edge.client;

import java.lang.RuntimeException;

/**
 * Access the underlying Nabto SDK error codes from exceptions.
 */
public interface NabtoException {

    /**
     * Get the Nabto SDK error code wrapper object.
     */
    public ErrorCode getErrorCode();
}
