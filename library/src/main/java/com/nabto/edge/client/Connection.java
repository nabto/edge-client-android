package com.nabto.edge.client;


/**
 * Connection API.
 *
 * Connection instances represents a Nabto Edge Direct connection between a client and a device.
 * The instance is used to create new reliable streams or CoAP sessions on top of a connection. The
 * Connection object must be kept alive for the duration of all streams, tunnels, and CoAP sessions
 * created from it.
 */
public interface Connection {
    /**
     * Connection type
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
     * @throws NabtoRuntimeException with error code NOT_CONNECTED if the connection is not connected
     * @throws NabtoRuntimeException with error code STOPPED if the connection is closed or stopped
     */
    String getDeviceFingerprint();

    /**
     * Get the fingerprint of the clients public key as a hex string.
     *
     * @return The client fingerprint encoded as hex.
     * @throws NabtoRuntimeException with error code INVALID_STATE if no private key is configured
     */
    String getClientFingerprint();

    /**
     * Get the connection type.
     *
     * @return The connection type
     * @throws NabtoRuntimeException with error code NOT_CONNECTED if the connection is not connected
     * @throws NabtoRuntimeException with error code STOPPED if the connection is closed or stopped
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
     * @throws NabtoRuntimeException with error code OPERATION_IN_PROGRESS if another close is in progreess.
     * @throws NabtoRuntimeException with error code STOPPED if the connection is closed or stopped or a parent object is stopped.
     * @throws NabtoRuntimeException with error code NOT_CONNECTED if the connection is not established yet.
     */
    void close();

    /**
     * Connect a connection.
     *
     * This function blocks until a connection is established or an
     * exception is thrown.
     *
     * @throws NabtoRuntimeException with error code STOPPED if the client instance was stopped
     * @throws NabtoNoChannelsException if a connection could not be established.
     */
    void connect();

    /**
     * Connect a connection, run callback once connection is established.
     * See the `connect()` function for error codes that the callback may give.
     *
     * @param callback The callback that will be run once the operation is done.
     */
    public void connectCallback(NabtoCallback callback);

    /**
     * Password authenticate a connection.
     *
     * This function blocks until the exchange is done or an exception
     * is thrown.
     *
     * @param username the username.
     * @param password the password
     * @throws NabtoRuntimeException with error code UNAUTHORIZED if the username or password is invalid
     * @throws NabtoRuntimeException with error code NOT_FOUND if the password authentication feature is not available on the device
     * @throws NabtoRuntimeException with error code NOT_CONNECTED if the connection is not open
     * @throws NabtoRuntimeException with error code OPERATION_IN_PROGRESS if a password authentication request is already in progress on the connection
     * @throws NabtoRuntimeException with error code TOO_MANY_REQUESTS if too many password attempts has been made
     * @throws NabtoRuntimeException with error code STOPPED if the client is stopped
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
