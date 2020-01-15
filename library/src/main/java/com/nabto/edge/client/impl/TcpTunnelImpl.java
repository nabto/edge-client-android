package com.nabto.edge.client.impl;

public class TcpTunnelImpl implements com.nabto.edge.client.TcpTunnel {

    com.nabto.edge.client.swig.TcpTunnel tcpTunnel;

    TcpTunnelImpl(com.nabto.edge.client.swig.TcpTunnel tcpTunnel) {
        this.tcpTunnel = tcpTunnel;
    }

    public void open(int localPort, String remoteHost, int remotePort)
    {
        try {
            tcpTunnel.open(localPort, remoteHost, remotePort).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void close() {
         try {
            tcpTunnel.close().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }
}
