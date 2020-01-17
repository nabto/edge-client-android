package com.nabto.edge.client;

public class NabtoNoChannelsException extends RuntimeException
{
    private ErrorCode localChannelErrorCode;
    private ErrorCode remoteChannelErrorCode;

    public NabtoNoChannelsException(int localChannelError, int remoteChannelError) {
        localChannelErrorCode = new ErrorCode(localChannelError);
        remoteChannelErrorCode = new ErrorCode(remoteChannelError);
    }

    public ErrorCode getLocalChannelErrorCode() {
        return localChannelErrorCode;
    }
    public ErrorCode getRemoteChannelErrorCode() {
        return remoteChannelErrorCode;
    }
}
