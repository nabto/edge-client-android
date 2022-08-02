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
     * 
     * Throws a NabtoRuntimeException with an INVALID_ARGUMENT error code
     * if the input is invalid.
     * 
     * @param json a string of valid json.
     */
    void updateOptions(String json);

    /**
     * Get connection options as a JSON document.
     *
     * Throws a NabtoRuntimeException with a FAILED error code
     * if the options could not be retrieved for some reason.
     * 
     * See nabto_client_connection_get_options for reference.
     * @return the current options encoded as a json object.
     */
    String getOptions();

    /**
     * Return the fingerprint of the device public key as a hex string.
     * 
     * Throws a NabtoRuntimeException with a INVALID_STATE error code
     * if the connection is not connected
     *
     * @return the device fungerprint encoded as hex
     */
    String getDeviceFingerprint();

    /**
     * Get the fingerprint of the clients public key as a hex string.
     * 
     * Throws a NabtoRuntimeException with a INVALID_STATE error code
     * if the connection is not connected
     * 
     * @return the client fingerprint encoded as hex.
     */
    String getClientFingerprint();

    /**
     * Get the connection type.
     * 
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * NOT_CONNECTED if the connection is not connected yet.
     * STOPPED if the connection is stopped or closed.
     * ```
     * 
     * @return the connection type
     */
    Type getType();

    /**
     * Enable the direct candidates feature for the connection.
     */
    void enableDirectCandidates();

    /**
     * Add a diect candidate.
     * 
     * Throws a NabtoRuntimeException with a INVALID_ARGUMENT error code
     * if the arguments is obviously invalid. e.g. using port number 0
     * 
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
     * 
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * OPERATION_IN_PROGRESS if another close is in progreess.
     * STOPPED if the connection is closed or stopped or a parent object is stopped.
     * NOT_CONNECTED if the connection is not established yet.
     * ```
     */
    void close();

    /**
     * Connect a connection.
     *
     * This function blocks until a connection is established or an
     * exception is thrown. If connecting fails for some reason, a NabtoRuntimeException will be thrown.
     * Use NabtoRuntimeException.getErrorCode() for further info.
     * 
     * The error code may be one of the following:
     * 
     * ```
     * UNAUTHORIZED if the authentication options do not match the basestation configuration
     * TOKEN_REJECTED if the basestation could not validate the specified token
     * STOPPED if the client instance was stopped
     * ```
     * 
     * Throws a NabtoNoChannelsException if all parameters input were accepted but a connection could not be
     * established. Details about what went wrong are available in the exception as
     *
     * ```
     * NabtoNoChannelsException.getLocalChannelErrorCode()
     * NabtoNoChannelsException.getRemoteChannelErrorCode()
     * NabtoNoChannelsException.getDirectCandidatesChannelErrorCode()
     * ```
     * 
     * The remote channel error code will be one of the following:
     * 
     * ```
     * NOT_ATTACHED if the target remote device is not attached to the basestation
     * FORBIDDEN if the basestation request is rejected
     * NONE if remote relay was not enabled
     * ```
     * 
     * The local channel error code will be one of the following:
     * 
     * ```
     * NONE if mDNS discovery was not enabled
     * NOT_FOUND if no local device was found
     * ```
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
     * is thrown. If authentication fails for some reason, a NabtoRuntimeException will be thrown.
     * Use NabtoRuntimeException.getErrorCode() for further info.
     * 
     * The error code may be one of the following:
     * 
     * ```
     * UNAUTHORIZED if the username or password is invalid
     * NOT_FOUND if the password authentication feature is not available on the device
     * NOT_CONNECTED if the connection is not open
     * OPERATION_IN_PROGRESS if a password authentication request is already in progress on the connection
     * TOO_MANY_REQUESTS if too many password attempts has been made
     * STOPPED if the client is stopped
     * ```
     * 
     * @param username the username.
     * @param password the password
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
     * @param connectionEventsCallback the connection events callback to add
     */
    void addConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);

    /**
     * Remove a listener for connection events.
     * @param connectionEventsCallback the connection events callback to remove.
     */
    void removeConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback);


}
