package com.nabto.edge.client;

import com.nabto.edge.client.NabtoException;

/**
 * Base class for Nabto specific runtime exceptions.
 */
public class NabtoRuntimeException extends RuntimeException implements NabtoException {
    private com.nabto.edge.client.swig.NabtoException origin;

    public NabtoRuntimeException(com.nabto.edge.client.swig.NabtoException e) {
        super(e.getMessage(), e);
        origin = e;
    }

    /**
     * Access the underlying Nabto Edge Client SDK error code.
     *
     * @return The underlying Nabto Edge Client SDK error code.
     */
    public ErrorCode getErrorCode() {
        return new ErrorCode(origin.status().getErrorCode());
    }
}
