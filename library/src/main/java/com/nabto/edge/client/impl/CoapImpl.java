package com.nabto.edge.client.impl;

import com.nabto.edge.client.NabtoCallback;
import com.nabto.edge.client.Coap;

public class CoapImpl implements Coap, AutoCloseable {
    private final CleanerService.Cleanable cleanable;
    private com.nabto.edge.client.swig.Coap coap;

    CoapImpl(com.nabto.edge.client.swig.Coap coap) {
        this.coap = coap;
        this.cleanable = createAndRegisterCleanable(this, coap);
    }

    /**
     * "Care must be taken not to capture the this instance, that’s why the creation has been
     * moved into a static method in the example above. Without a this in scope, it can’t be
     * captured by accident." (from https://stackoverflow.com/questions/46144524/delete-native-peer-with-general-phantomreference-class/47830289#47830289)
     */
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, com.nabto.edge.client.swig.Coap nativeHandle) {
        return CleanerService.instance().register(o, () -> nativeHandle.delete());
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
        coap.execute().callback(Util.makeFutureCallback(callback));
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

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
}
