package com.nabto.edge.client;

/**
 * Callback interface specifically for Connection.connectCallback()
 */
public interface ConnectCallback {

    /**
     * Function called when the connectCallback() is finished.
     * 
     * @param error The resulting error code, may be OK, STOPPED or NO_CHANNELS
     * @param localChannelError Error code on local channel, OK unless error is NO_CHANNELS
     * @param remoteChannelError Error code on local channel, OK unless error is NO_CHANNELS
     * @param remoteCandidatesError Error code on local channel, OK unless error is NO_CHANNELS
     */
    void run(int error, int localChannelError, int remoteChannelError, int remoteCandidatesError);
}
