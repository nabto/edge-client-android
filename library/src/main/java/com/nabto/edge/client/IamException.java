package com.nabto.edge.client;

import java.lang.RuntimeException;

public class IamException extends RuntimeException {
    private IamError error;

    public IamException(IamError error) {
        super(error.name());
        this.error = error;
    }

    public IamError getError() {
        return error;
    }
}
