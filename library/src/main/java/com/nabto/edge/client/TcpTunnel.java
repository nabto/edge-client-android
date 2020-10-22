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
    public void open(String service, int localPort);

    /**
     * Close a tunnel, this function blocks until the tunnel is closed
     */
    public void close();

    /**
     * Get the local port whic the tunnel is bound to.
     * If the tunnel is not opened an exception is thrown
     *
     * @return the local port number used.
     */
    public int getLocalPort();
}
