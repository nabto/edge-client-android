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
     *
     * Possible local channel error code are:
     *
     * - `NOT_FOUND` if the device was not found locally
     * - `NONE` if mDNS discovery was not enabled
     *
     * @return the local channel error code
     */
    public ErrorCode getLocalChannelErrorCode() {
        return localChannelErrorCode;
    }

    /**
     * Get underlying error code on remote channel.
     *
     * Possible remote channel error code are:
     *
     * - `NOT_ATTACHED` if the target remote device is not attached to the basestation
     * - `TIMEOUT` if a timeout occured when connecting to the basestation.
     * - `FORBIDDEN` if the basestation request is rejected
     * - `TOKEN_REJECTED` if the basestation rejected based on an invalid SCT or JWT
     * - `DNS` if the server URL failed to resolve
     * - `UNKNOWN_SERVER_KEY` if the provided server key was not known by the basestation
     * - `UNKNOWN_PRODUCT_ID` if the provided product ID was not known by the basestation
     * - `UNKNOWN_DEVICE_ID` if the provided device ID was not known by the basestation
     * - `NONE` if remote relay was not enabled
     *
     * @return the remote channel error code
     */
    public ErrorCode getRemoteChannelErrorCode() {
        return remoteChannelErrorCode;
    }

    /**
     * Get the direct channel error code.
     *
     * Possible direct channel error code are:
     *
     * - `NOT_FOUND` if no responses was received on any added direct channels
     * - `NONE` if direct channels was not enabled
     *
     * @return the direct channel error code
     */
    public ErrorCode getDirectCandidatesChannelErrorCode() {
        return directCandidatesChannelErrorCode;
    }
}
