package com.nabto.edge.client;


/**
 * Connection API.
 *
 * Connection instances represents a Nabto Edge Direct connection between a client and a device.
 * The instance is used to create new reliable streams or CoAP sessions on top of a connection.
 */
public interface Connection {

    /**
     * See nabto_client_connection_set_options for reference.
     *
     * update the options for a connection, the provided options is
     * merged with the already set options.
     *
     * @param json a string of valid json.
     */
    void updateOptions(String json);

    /**
     * See nabto_client_connection_get_options for reference.
     *
     *
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
