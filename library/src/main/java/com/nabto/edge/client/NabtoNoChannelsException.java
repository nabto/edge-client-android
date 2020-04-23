package com.nabto.edge.client;

/**
 * This exception is thrown if a channel could not be opened to a device.
 */
public class NabtoNoChannelsException extends RuntimeException {
    private ErrorCode localChannelErrorCode;
    private ErrorCode remoteChannelErrorCode;

    public NabtoNoChannelsException(int localChannelError, int remoteChannelError) {
        localChannelErrorCode = new ErrorCode(localChannelError);
        remoteChannelErrorCode = new ErrorCode(remoteChannelError);
    }

    /**
     * Get underlying error code on local channel.
     */
    public ErrorCode getLocalChannelErrorCode() {
        return localChannelErrorCode;
    }

    /**
     * Get underlying error code on remote channel.
     */
    public ErrorCode getRemoteChannelErrorCode() {
        return remoteChannelErrorCode;
    }
}
