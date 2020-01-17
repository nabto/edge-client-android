package com.nabto.edge.client;

import com.nabto.edge.client.NabtoException;

public class NabtoRuntimeException extends RuntimeException implements NabtoException
{
    private com.nabto.edge.client.swig.NabtoException origin;

    public NabtoRuntimeException(com.nabto.edge.client.swig.NabtoException e)
    {
        super(e.getMessage(), e);
        origin = e;
    }

    public ErrorCode getErrorCode() {
        return new ErrorCode(origin.status().getErrorCode());
    }
}
