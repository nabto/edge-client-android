package com.nabto.edge.client;

import java.lang.RuntimeException;

public class NabtoException extends RuntimeException {
    private String description;
    private String name;
    private com.nabto.edge.client.swig.NabtoException origin;
    public NabtoException(com.nabto.edge.client.swig.NabtoException e)
    {
        description = e.status().getDescription();
        name = e.status().getName();
        origin = e;
    }

    public String getDescription()  {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return origin.getMessage();
    }
}
