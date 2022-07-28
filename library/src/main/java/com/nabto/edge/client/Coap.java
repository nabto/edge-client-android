package com.nabto.edge.client;

/**
 * The CoAP API.
 *
 * The CoAP API allows exchange of CoAP message on top of a Nabto connection between a client and
 * device. This is conceptually similar to Nabto 4 RPC but much more robust and complete.
 *
 * Coap instances are created using the Connection.createCoap() factory method.
 * The Coap object must be kept alive while in use.
 */
public interface Coap {

    /**
     * Some of the most used coap content formats from
     * https://www.iana.org/assignments/core-parameters/core-parameters.xhtml
     */
    public class ContentFormat {

        /**
         * Plain text content.
         */
        public static final int TEXT_PLAIN = 0;

        /**
         * XML content.
         */
        public static final int APPLICATION_XML = 41;

        /**
         * Binary data.
         */
        public static final int APPLICATION_OCTET_STREAM = 42;

        /**
         * Concise Binary Object Representation (compact data representation inspired by JSON).
         */
        public static final int APPLICATION_CBOR = 60;
    }

    /**
     * Set the payload for the request.
     *
     * @param contentFormat a contentformat.
     * @param payload the payload.
     */
    void setRequestPayload(int contentFormat, byte[] payload);

    /**
     * Executes the request and blocks until the request is complete.
     * 
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * TIMEOUT if the request timed out (took more than 2 minutes.)
     * STOPPED if the coap request or a parent object is stopped.
     * NOT_CONNECTED if the connection is not established yet.
     * ```
     */
    void execute();

    /**
     * Executes request asynchronously with a callback
     * See execute() for error codes.
     *
     * @param callback The callback that will be run once the request has been executed.
     */
    void executeCallback(NabtoCallback callback);

    /**
     * Return status code returned from the server e.g. 204.
     *
     * Throws a NabtoRuntimeException with a INVALID_STATE error code.
     * if there’s no response yet.
     * 
     * @return the status code, e.g. 204
     */
    int getResponseStatusCode();

    /**
     * Get response content format.
     *
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * NO_DATA if the response does not have a content format.
     * INVALID_STATE if no response is ready yet.
     * ```
     * 
     * @return the response content format or -1 if no content format is set in the response.
     */
    int getResponseContentFormat();

    /**
     * Get response payload
     *
     * May throw a NabtoRuntimeException with one of the following error codes:
     * ```
     * NO_DATA if the response does not have a payload.
     * INVALID_STATE if no response is ready yet.
     * ```
     * 
     * @return The payload. If the response has no payload, the empty buffer is returned.
     */
    byte[] getResponsePayload();
}
