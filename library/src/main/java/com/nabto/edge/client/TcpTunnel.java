package com.nabto.edge.client;

public interface TcpTunnel {

    /**
     * Open a tunnel
     */
    public void open(int localPort, String remoteHost, int remotePort);

    /**
     * Close a tunnel, this function blocks until the tunnel is closed
     */
    public void close();
}
