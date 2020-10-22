package com.nabto.edge.client;

/**
 * This exception is thrown if a channel could not be opened to a device.
 */
public class NabtoNoChannelsException extends RuntimeException {
    private ErrorCode localChannelErrorCode;
    private ErrorCode remoteChannelErrorCode;
    private ErrorCode directCandidatesChannelErrorCode;

    public NabtoNoChannelsException(int localChannelError, int remoteChannelError, int directCandidatesChannelError) {
        localChannelErrorCode = new ErrorCode(localChannelError);
        remoteChannelErrorCode = new ErrorCode(remoteChannelError);
        directCandidatesChannelErrorCode = new ErrorCode(directCandidatesChannelError);
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

    public ErrorCode getDirectCandidatesChannelErrorCode() {
        return directCandidatesChannelErrorCode;
    }
}
