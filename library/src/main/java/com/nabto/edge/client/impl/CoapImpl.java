package com.nabto.edge.client.impl;

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
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public void execute()
    {
        try {
            coap.execute().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }
    public int getResponseStatusCode()
    {
        try {
            return coap.getResponseStatusCode();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }
    public int getResponseContentFormat()
    {
        try {
            return coap.getResponseContentFormat();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }
    public byte[] getResponsePayload()
    {
        try {
            return coap.getResponsePayload();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }


}
