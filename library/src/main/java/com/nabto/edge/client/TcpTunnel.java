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
public interface TcpTunnel extends AutoCloseable {

    /**
     * Open this tunnel. Blocks until the tunnel is ready to use or an error occurs.
     *
     * @param service The service to connect to on the remote device (as defined in the device's
     * configuration), e.g. "http", "http-admin", "ssh", "rtsp".
     * @param localPort The local port to listen on. If 0 is specified, an ephemeral port is used,
     * it can be retrieved with `getLocalPort()`.
     * @throws NabtoRuntimeException with error code `FORBIDDEN` if the device did not allow opening the tunnel.
     * @throws NabtoRuntimeException with error code `STOPPED` if the tunnel or a parent object is stopped.
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
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
     * Close a tunnel without blocking.
     *
     * Note: Odd name is to distinguish from AutoCloseable's close() with very different
     * semantics.
     *
     * @param callback The callback that will be run once the tunnel is closed.
     */
    public void tunnelCloseCallback(NabtoCallback callback);

    /**
     * Get the local port which the tunnel is bound to.
     *
     * @throws NabtoRuntimeException with error code `INVALID_STATE` if the tunnel is not open.
     * @return the local port number used.
     */
    public int getLocalPort();

    /**
     * Close a tunnel, this function blocks until the tunnel is closed
     *
     * Note: Odd name is to distinguish from AutoCloseable's close() with very different
     * semantics.
     */
    public void tunnelClose();

    /**
     * Note! Semantics have changed with version 3.0 due to name clash with AutoCloseable!
     * The 2.x version of close() has been renamed to tunnelClose().
     *
     * This function releases any resources associated with the TcpTunnel instance. This method is
     * called automatically at the end of a try-with-resources block, which
     * helps to ensure that resources are released promptly and reliably.
     *
     * <p>Example of using a CoAP object within a try-with-resources statement:</p>
     * <pre>
     * try (TcpTunnel tunnel = connection.createTunnel(...)) {
     *     // ... use stream
     * }
     * </pre>
     *
     * <p>With this setup, {@code close()} will be called automatically on
     * {@code connect} at the end of the block, releasing any underlying
     * native Nabto Client SDK resources without any further action required on the application.</p>
     *
     * <p>Unlike the {@link AutoCloseable#close()} method, this {@code close()}
     * method does not throw any exceptions.</p>
     */
    @Override
    void close();

}
