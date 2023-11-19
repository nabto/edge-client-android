package com.nabto.edge.iamutil;

import java.lang.RuntimeException;

/**
 * Runtime exception wrapper for IamError codes
 */
public class IamException extends RuntimeException {
    private IamError error;

    public IamException(IamError error) {
        super(error.name());
        this.error = error;
    }

    public IamException(IamError error, String msg) {
        super(error.name() + " :: " + msg);
        this.error = error;
    }

    /**
     * Get the IAM error.
     *
     * @return The underlying IAM error.
     */
    public IamError getError() {
        return error;
    }
}
