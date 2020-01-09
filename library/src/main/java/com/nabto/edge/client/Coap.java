package com.nabto.edge.client;

public interface Coap {

    /**
     * Some of the most used coap content formats from
     * https://www.iana.org/assignments/core-parameters/core-parameters.xhtml
     */
    public class ContentFormat {
        public static final int TEXT_PLAIN = 0;
        public static final int APPLICATION_XML = 41;
        public static final int APPLICATION_OCTET_STREAM = 42;
        public static final int APPLICATION_CBOR = 60;

    }

    /**
     * Set the payload for the request.
     *
     * @param contentFormat a contentformat.
     * @param paylaod the payload.
     */
    void setRequestPayload(int contentFormat, byte[] payload);

    /**
     * Blocking while request is being executed
     */
    void execute();

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
