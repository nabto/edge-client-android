package com.nabto.edge.client;

public class NabtoCheckedException extends Exception implements NabtoException {
    private com.nabto.edge.client.swig.NabtoException origin;

    public NabtoCheckedException(com.nabto.edge.client.swig.NabtoException e)
    {
        super(e.getMessage(), e);
        origin = e;
    }

    public ErrorCode getErrorCode() {
        return new ErrorCode(origin.status().getErrorCode());
    }
}
