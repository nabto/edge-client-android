package com.nabto.edge.client;

public class NabtoConnectFailedException extends RuntimeException
{
    private ErrorCode mdnsChannelErrorCode;
    private ErrorCode udpRelayChannelErrorCode;

    public NabtoConnectFailedException(int mdnsChannelError, int udpRelayChannelError) {
        mdnsChannelErrorCode = new ErrorCode(mdnsChannelError);
        udpRelayChannelErrorCode = new ErrorCode(udpRelayChannelError);
    }

    public ErrorCode getMdnsChannelErrorCode() {
        return mdnsChannelErrorCode;
    }
    public ErrorCode getUdpRelayChannelErrorCode() {
        return udpRelayChannelErrorCode;
    }
}
