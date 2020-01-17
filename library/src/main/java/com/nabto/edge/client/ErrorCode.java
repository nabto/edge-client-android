package com.nabto.edge.client;

import com.nabto.edge.client.swig.Status;

public class ErrorCode {
    private Status status;

    public ErrorCode(int errorCode) {
        status = new Status(errorCode);
    }

    public String getDescription() {
        return status.getDescription();
    }

    public String getName() {
        return status.getName();
    }

    public int getErrorCode() {
        return status.getErrorCode();
    }
}
