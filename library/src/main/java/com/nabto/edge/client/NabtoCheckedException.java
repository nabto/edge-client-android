package com.nabto.edge.client;

public class NabtoCheckedException extends Exception implements NabtoException {
    private com.nabto.edge.client.swig.NabtoException origin;

    public NabtoCheckedException(com.nabto.edge.client.swig.NabtoException e)
    {
        origin = e;
    }

    public String getDescription()  {
        return origin.status().getDescription();
    }

    public String getName() {
        return origin.status().getName();
    }

    public String getMessage() {
        return origin.getMessage();
    }

    public int getErrorCode() {
        return origin.status().getErrorCode();
    }
}
