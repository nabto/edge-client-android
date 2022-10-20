package com.nabto.edge.client;

import com.nabto.edge.client.swig.Status;

/**
 * This class wraps Nabto Edge SDK error codes.
 */
public class ErrorCode {
    private Status status;

    public ErrorCode(int errorCode) {
        status = new Status(errorCode);
    }

    /**
     * Get error description
     *
     * @return String describing the error
     */
    public String getDescription() {
        return status.getDescription();
    }

    /**
     * Get the name of the error.
     *
     * @return The error name
     */
    public String getName() {
        return status.getName();
    }

    /**
     * Get the error code.
     *
     * The error code can be one on the values listed in ErrorCodes
     *
     * @return The error code
     */
    public int getErrorCode() {
        return status.getErrorCode();
    }
}
