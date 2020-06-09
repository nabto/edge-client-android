package com.nabto.edge.client;


/**
 * Connection API.
 *
 * Connection instances represents a Nabto Edge Direct connection between a client and a device.
 * The instance is used to create new reliable streams or CoAP sessions on top of a connection.
 */
public interface Connection {

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
     */
    String getOptions();

    /**
     * Return the fingerprint of the device public key as a hex string.
     *
     * If the connection is not connected this function throws an
     * exception.
     */
    String getDeviceFingerprintHex();

    /**
     * Get the fingerprint of the clients public key as a hex string.
     */
    String getClientFingerprintHex();

    /**
     * Create stream.
     */
    Stream createStream();

    /**
     * Create a coap request/response object.
     */
    Coap createCoap(String method, String path);

    /**
     * Create a TCP tunnel.
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
     * Add a listener for connection events.
     */
    void addConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);

    /**
     * Remove a listener for connection events.
     */
    void removeConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);
}
