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
     * Blocking while request is being executed
     */
    void execute();

    /**
     * Executes request asynchronously with a callback
     */
    void executeCallback(NabtoCallback callback);

    /**
     * Return status code returned from the server e.g. 204.
     *
     * @return the status code, e.g. 204
     */
    int getResponseStatusCode();

    /**
     * Get response content format.
     *
     * @return the response content format or -1 if no content format is set in the response.
     */
    int getResponseContentFormat();

    /**
     * Get response payload
     *
     * @return The payload. If the response has no payload, the empty buffer is returned.
     */
    byte[] getResponsePayload();
}
