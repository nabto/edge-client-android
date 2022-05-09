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
     * ```
     * ProductId
     * DeviceId
     * PrivateKey
     * ServerUrl
     * ServerKey
     * ServerJwtToken
     * ServerConnectToken
     * AppName
     * AppVersion
     * ```
     * @param json a string of valid json.
     */
    void updateOptions(String json);

    /**
     * Get connection options as a JSON document.
     *
     * See nabto_client_connection_get_options for reference.
     * @return the current options encoded as a json object.
     */
    String getOptions();

    /**
     * Return the fingerprint of the device public key as a hex string.
     *
     * If the connection is not connected this function throws an
     * exception.
     * @return the device fungerprint encoded as hex
     */
    String getDeviceFingerprint();

    /**
     * Get the fingerprint of the clients public key as a hex string.
     * @return the client fingerprint encoded as hex.
     */
    String getClientFingerprint();

    /**
     * Get the connection type.
     * @return the connection type
     */
    Type getType();

    /**
     * Enable the direct candidates feature for the connection.
     */
    void enableDirectCandidates();

    /**
     * Add a diect candidate.
     * @param host the hest either as ip or a resolveable name.
     * @param port the port.
     */
    void addDirectCandidate(String host, int port);

    /**
     * Mark the end of direct candidates,
     */
    void endOfDirectCandidates();

    /**
     * Create stream. The returned Stream object must be kept alive while in use.
     * @return the created stream.
     */
    Stream createStream();

    /**
     * Create a coap request/response object. The returned Coap object must be kep alive while in use.
     * @param method e.g. GET, POST or PUT
     * @param path the path e.g. /hello-world
     * @return the created coap object.
     */
    Coap createCoap(String method, String path);

    /**
     * Create a TCP tunnel. The returned TcpTunnel object must be kept alive while in use.
     * @return the created tcp tunnel.
     */
    TcpTunnel createTcpTunnel();

    /**
     * Close a connection.
     */
    void close();

    /**
     * Connect a connection.
     *
     * This function blocks until a connection is established or an
     * exception is thrown.
     */
    void connect();

    /**
     * Password authenticate a connectio.
     *
     * This function blocks until the exchange is done an throws an exception
     * if the password authentication fails.
     * @param username the username.
     * @param password the password
     */
    void passwordAuthenticate(String username, String password);

    /**
     * Add a listener for connection events.
     * @param connectionEventsCallback the connection events callback to add
     */
    void addConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);

    /**
     * Remove a listener for connection events.
     * @param connectionEventsCallback the connection events callback to remove.
     */
    void removeConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);


}
