package com.nabto.edge.client;

/**
 * This exception is thrown if EOF is reached.
 */
public class NabtoEOFException extends NabtoCheckedException {
    public NabtoEOFException(com.nabto.edge.client.swig.NabtoException e) {
        super(e);
    }
}
