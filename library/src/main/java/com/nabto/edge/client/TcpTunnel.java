package com.nabto.edge.client;

/**
 * The TCP Tunnel API.
 *
 * The TCP Tunnel API is a high level wrapper for streaming, allowing applications to tunnel traffic
 * through Nabto by integrating through a simple TCP socket, just like e.g. SSH tunnels. TCP Tunnels
 * can hence be used to quickly add remote access capabilities to existing applications that already
 * support TCP communication.
 *
 * TCP Tunnel instances are created using the Connection.createTcpTunnel() factory method.
 */
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
