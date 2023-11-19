package com.nabto.edge.client.impl;

import com.nabto.edge.client.NabtoCallback;

public class TcpTunnelImpl implements com.nabto.edge.client.TcpTunnel {

    com.nabto.edge.client.swig.TcpTunnel tcpTunnel;
    private final CleanerService.Cleanable cleanable;

    TcpTunnelImpl(com.nabto.edge.client.swig.TcpTunnel tcpTunnel) {
        this.tcpTunnel = tcpTunnel;
        this.cleanable = createAndRegisterCleanable(this, tcpTunnel);
    }

    public void open(String service, int localPort)
    {
        try {
            tcpTunnel.open(service, localPort).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void openCallback(String service, int localPort, NabtoCallback callback)
    {
        try {
            tcpTunnel.open(service, localPort).callback(Util.makeFutureCallback(callback));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    @Override
    public void close() {
       cleanable.clean();
    }

    public void tunnelClose() {
        try {
            tcpTunnel.close().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void tunnelCloseCallback(NabtoCallback callback) {
        try {
            tcpTunnel.close().callback(Util.makeFutureCallback(callback));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public int getLocalPort() {
        try {
            return tcpTunnel.getLocalPort();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    /// static helper to ensure no "this" is captured accidentally
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, com.nabto.edge.client.swig.TcpTunnel nativeHandle) {
        return CleanerService.instance().register(o, () -> nativeHandle.delete());
    }

}
