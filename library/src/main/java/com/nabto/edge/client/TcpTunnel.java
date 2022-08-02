package com.nabto.edge.client;

/**
 * The TCP Tunnel API.
 *
 * The TCP Tunnel API is a high level wrapper for streaming, allowing applications to tunnel traffic
 * through Nabto by integrating through a simple TCP socket, just like e.g. SSH tunnels. TCP Tunnels
 * can hence be used to quickly add remote access capabilities to existing applications that already
 * support TCP communication.
 *
 * The client opens a TCP listener which listens for incoming TCP connections on the local
 * port. When a connection is accepted by the TCP listener, a new stream is created to the
 * device. When the stream is created on the device, the device opens a TCP connection to the
 * specified service. Once this connection is opened, TCP data flows from the TCP client on the
 * client side to the TCP server on the device side.
 *
 * TCP Tunnel instances are created using the Connection.createTcpTunnel() factory method.
 * The TcpTunnel object must be kept alive while in use.
 */
public interface TcpTunnel {

    /**
     * Open this tunnel. Blocks until the tunnel is ready to use or an error occurs.
     *
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * NOT_FOUND if requesting an unknown service
     * FORBIDDEN if target device did not allow opening a tunnel to specified service for the current client
     * STOPPED if the tunnel is stopped.
     * NOT_CONNECTED if the connection is not established yet.
     * ```
     * 
     * @param service The service to connect to on the remote device (as defined in the device's
     * configuration), e.g. "http", "http-admin", "ssh", "rtsp".
     * @param localPort The local port to listen on. If 0 is specified, an ephemeral port is used,
     * it can be retrieved with `getLocalPort()`.
     */
    public void open(String service, int localPort);

    /**
     * Open this tunnel without blocking.
     * See TcpTunnel.open() for error codes.
     *
     * @param service The service to connect to on the remote device (as defined in the device's
     * configuration), e.g. "http", "http-admin", "ssh", "rtsp".
     * @param localPort The local port to listen on. If 0 is specified, an ephemeral port is used,
     * it can be retrieved with `getLocalPort()`.
     * @param callback The callback that will be run once the tunnel is opened.
     */
    public void openCallback(String service, int localPort, NabtoCallback callback);

    /**
     * Close a tunnel, this function blocks until the tunnel is closed
     */
    public void close();

    /**
     * Close a tunnel without blocking.
     *
     * @param callback The callback that will be run once the tunnel is closed.
     */
    public void closeCallback(NabtoCallback callback);

    /**
     * Get the local port whic the tunnel is bound to.
     * 
     * Throws a NabtoRuntimeException with a INVALID_STATE error code
     * if the tunnel is not opened.
     * 
     * 
     * @return the local port number used.
     */
    public int getLocalPort();
}
