package com.nabto.edge.client.impl;

import com.nabto.edge.client.NabtoCallback;
import com.nabto.edge.client.Coap;

public class CoapImpl implements Coap {

    private com.nabto.edge.client.swig.Coap coap;

    CoapImpl(com.nabto.edge.client.swig.Coap coap) {
        this.coap = coap;
    }

    public void setRequestPayload(int contentFormat, byte[] payload)
    {
        try {
            coap.setRequestPayload(contentFormat, payload);
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void execute()
    {
        try {
            coap.execute().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void executeCallback(NabtoCallback callback)
    {
        com.nabto.edge.client.swig.FutureCallback cb = new com.nabto.edge.client.swig.FutureCallback() {
            public void run(com.nabto.edge.client.swig.Status status) {
                callback.run(status.getErrorCode(), null);
            }
        };
        coap.execute().callback(cb);
    }

    public int getResponseStatusCode()
    {
        try {
            return coap.getResponseStatusCode();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }

    }
    public int getResponseContentFormat()
    {
        try {
            return coap.getResponseContentFormat();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }

    }
    public byte[] getResponsePayload()
    {
        try {
            return coap.getResponsePayload();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }

    }


}
