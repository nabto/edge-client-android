package com.nabto.edge.client;

import java.lang.RuntimeException;

public interface NabtoException {

    public String getDescription();
    public String getName();

    public String getMessage();

    public int getErrorCode();
}
