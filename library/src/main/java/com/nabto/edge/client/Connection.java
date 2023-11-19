package com.nabto.edge.client;


/**
 * Connection API.
 *
 * Connection instances represents a Nabto Edge Direct connection between a client and a device.
 * The instance is used to create new reliable streams or CoAP sessions on top of a connection. The
 * Connection object must be kept alive for the duration of all streams, tunnels, and CoAP sessions
 * created from it.
 */
public interface Connection extends AutoCloseable {
    /**
     * Connection types
     *
     * - `RELAY`: Relay connection through a Nabto Basestation
     * - `DIRECT`: Direct connection either local or p2p
     */
    public enum Type {
        RELAY,
        DIRECT
    }

    /**
     * Set connection parameters through a JSON document.
     *
     * The provided options are merged with the already set options. n error is returned if a
     * parameter is not recognized. This only updates the internal representation of parameters.
     *
     * Options are set in a JSON document as follows:
     * ```
     * {
     *   "ProductId": "pr-12345678",
     *   "DeviceId": "de-12345678",
     *   "ServerUrl": "https://pr-12345678.clients.nabto.net",
     *   "ServerKey": "sk-12345678123456781234567812345678"
     * }
     * ```
     *
     * The following options are supported:
     *
     * - `ProductId`
     * - `DeviceId`
     * - `PrivateKey`
     * - `ServerUrl`
     * - `ServerKey`
     * - `ServerJwtToken`
     * - `ServerConnectToken`
     * - `AppName`
     * - `AppVersion`
     *
     * @param json a string of valid json.
     */
    void updateOptions(String json);

    /**
     * Get connection options as a JSON document.
     *
     * The returned document is similar to that accepted by `updateOptions()`
     * except the private key is not exposed.
     *
     * @return The current options encoded as a json object.
     */
    String getOptions();

    /**
     * Return the fingerprint of the device public key as a hex string.
     *
     * @return The device fungerprint encoded as hex
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not connected
     * @throws NabtoRuntimeException with error code `STOPPED` if the connection is closed or stopped
     */
    String getDeviceFingerprint();

    /**
     * Get the fingerprint of the clients public key as a hex string.
     *
     * @return The client fingerprint encoded as hex.
     * @throws NabtoRuntimeException with error code `INVALID_STATE` if no private key is configured
     */
    String getClientFingerprint();

    /**
     * Get the connection type.
     *
     * @return The connection type
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not connected
     * @throws NabtoRuntimeException with error code `STOPPED` if the connection is closed or stopped
     */
    Type getType();

    /**
     * Enable the direct candidates feature for the connection.
     */
    void enableDirectCandidates();

    /**
     * Add a direct candidate.
     *
     * @param host The host either as IP or a resolveable hostname.
     * @param port A valid port number.
     */
    void addDirectCandidate(String host, int port);

    /**
     * Mark the end of direct candidates indicating that no more candidates will
     * be added to the connection.
     */
    void endOfDirectCandidates();

    /**
     * Create stream. The returned Stream object must be kept alive while in use.
     *
     * @return The created stream.
     */
    Stream createStream();

    /**
     * Create a coap request/response object. The returned Coap object must be kept alive while in use.
     *
     * @param method CoAP request method e.g. GET, POST or PUT
     * @param path CoAP request path e.g. /hello-world
     * @return the created coap object.
     */
    Coap createCoap(String method, String path);

    /**
     * Create a TCP tunnel. The returned TcpTunnel object must be kept alive while in use.
     *
     * @return The created TCP tunnel.
     */
    TcpTunnel createTcpTunnel();

    /**
     * Close a connection.
     *
     * Note: Odd name is to distinguish from AutoCloseable's close() with very different
     * semantics.
     *
     * @throws NabtoRuntimeException with error code `OPERATION_IN_PROGRESS` if another close is in progreess.
     * @throws NabtoRuntimeException with error code `STOPPED` if the connection is closed or stopped or a parent object is stopped.
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
     */
    void connectionClose();

    /**
     * Note! Semantics have changed with version 3.0 due to name clash with AutoCloseable!
     * The 2.x version of close() has been renamed to connectionClose().
     *
     * This function releases any resources associated with the Connection instance. This method is
     * called automatically at the end of a try-with-resources block, which
     * helps to ensure that resources are released promptly and reliably.
     *
     * <p>Example of using a CoAP object within a try-with-resources statement:</p>
     * <pre>
     * try (Connection connection = client.createConnect(...)) {
     *     // ... use connection
     * }
     * </pre>
     *
     * <p>With this setup, {@code close()} will be called automatically on
     * {@code connect} at the end of the block, releasing any underlying
     * native Nabto Client SDK resources without any further action required on the application.</p>
     *
     * <p></p>If the try-with-resources construct is not feasible, the application must manually call close()
     * when the Connection instance is no longer needed.</p>
     *
     * <p>Unlike the {@link AutoCloseable#close()} method, this {@code close()}
     * method does not throw any exceptions.</p>
     */
    @Override
    void close();

    /**
     * Establish a connection.
     *
     * This function blocks until a connection is established or an
     * exception is thrown.
     *
     * @throws NabtoRuntimeException with error code `STOPPED` if the client instance was stopped
     * @throws NabtoNoChannelsException if a connection could not be established.
     */
    void connect();

    /**
     * Establish a connection, run callback once connection is established.
     * See the `connect()` function for error codes that the callback may give.
     *
     * @param callback The callback that will be run once the operation is done.
     */
    public void connectCallback(NabtoCallback callback);

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
    public ErrorCode getLocalChannelErrorCode();

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
    public ErrorCode getRemoteChannelErrorCode();
    
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
    public ErrorCode getDirectCandidatesChannelErrorCode();

    /**
     * Password authenticate a connection.
     *
     * This function blocks until the exchange is done or an exception
     * is thrown.
     *
     * @param username the username.
     * @param password the password
     * @throws NabtoRuntimeException with error code `UNAUTHORIZED` if the username or password is invalid
     * @throws NabtoRuntimeException with error code `NOT_FOUND` if the password authentication feature is not available on the device
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not open
     * @throws NabtoRuntimeException with error code `OPERATION_IN_PROGRESS` if a password authentication request is already in progress on the connection
     * @throws NabtoRuntimeException with error code `TOO_MANY_REQUESTS` if too many password attempts has been made
     * @throws NabtoRuntimeException with error code `STOPPED` if the client is stopped
     */
    void passwordAuthenticate(String username, String password);

    /**
     * Password authenticate a connection, run callback once connection is established.
     * See the `passwordAuthenticate()` function for error codes that the callback may give.
     *
     * @param username The username.
     * @param password The password.
     * @param callback The callback that will be run once the operation is done.
     */
    void passwordAuthenticateCallback(String username, String password, NabtoCallback callback);

    /**
     * Add a listener for connection events.
     *
     * @param connectionEventsCallback the connection events callback to add
     */
    void addConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);

    /**
     * Remove a listener for connection events.
     *
     * @param connectionEventsCallback the connection events callback to remove.
     */
    void removeConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);

}
