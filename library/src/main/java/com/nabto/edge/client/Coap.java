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
public interface Coap extends AutoCloseable {

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
     * @param contentFormat Contentformat of the payload.
     * @param payload The payload to set.
     */
    void setRequestPayload(int contentFormat, byte[] payload);

    /**
     * Executes the request and blocks until the request is complete.
     *
     * @throws NabtoRuntimeException with error code `TIMEOUT` if the request timed out (took more than 2 minutes.)
     * @throws NabtoRuntimeException with error code `STOPPED` if the coap request or a parent object is stopped.
     * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
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
     * @throws NabtoRuntimeException with error code `INVALID_STATE` if the response is not available.
     * @return the status code, e.g. 204
     */
    int getResponseStatusCode();

    /**
     * Get response content format.
     *
     * @throws NabtoRuntimeException with error code `NO_DATA` if the response does not include a content format.
     * @throws NabtoRuntimeException with error code `INVALID_STATE` if the response is not available.
     * @return the response content format or -1 if no content format is set in the response.
     */
    int getResponseContentFormat();

    /**
     * Get response payload
     *
     * @throws NabtoRuntimeException with error code `NO_DATA` if the response does not include a content format.
     * @throws NabtoRuntimeException with error code `INVALID_STATE` if the response is not available.
     * @return The payload. If the response has no payload, the empty buffer is returned.
     */
    byte[] getResponsePayload();

    /**
     * Releases any resources associated with the CoAP instance. This method is
     * called automatically at the end of a try-with-resources block, which
     * helps to ensure that resources are released promptly and reliably.
     *
     * <p>Example of using a CoAP object within a try-with-resources statement:</p>
     * <pre>
     * try (Coap coap = connection.createCoap(...)) {
     *     // ... use coap
     * }
     * </pre>
     *
     * <p>With this setup, {@code close()} will be called automatically on
     * {@code coap} at the end of the block, releasing any underlying
     * native Nabto Client SDK resources without any further action required on the application.</p>
     *
     * <p>Unlike the {@link AutoCloseable#close()} method, this {@code close()}
     * method does not throw any exceptions.</p>
     */
    @Override
    void close();
}
