package com.nabto.edge.client;

import java.lang.RuntimeException;

/**
 * Access the underlying Nabto SDK error codes from exceptions.
 */
public interface NabtoException {

    /**
     * Get the Nabto SDK error code wrapper object.
     *
     * @return The underlying Nabto Edge Client SDK error code.
     */
    public ErrorCode getErrorCode();
}
