package com.nabto.edge.client;

/**
 * Base class for Nabto specific checked exceptions.
 */
public class NabtoCheckedException extends Exception implements NabtoException {
    private com.nabto.edge.client.swig.NabtoException origin;

    public NabtoCheckedException(com.nabto.edge.client.swig.NabtoException e) {
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
