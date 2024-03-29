package com.nabto.edge.client.impl;

import com.nabto.edge.client.MdnsResultListener;

@Deprecated
public class MdnsResultScanner extends com.nabto.edge.client.swig.FutureCallback implements AutoCloseable {

    private com.nabto.edge.client.swig.Context context;
    private com.nabto.edge.client.swig.MdnsResolver resolver;
    private com.nabto.edge.client.swig.FutureMdnsResult resultFuture;
    private com.nabto.edge.client.MdnsResultListener listener;
    private final CleanerService.Cleanable cleanable;

    MdnsResultScanner(com.nabto.edge.client.swig.Context context, MdnsResultListener listener, String subtype)
    {
        this.context = context;
        this.listener = listener;
        resolver = context.createMdnsResolver(subtype);
        this.cleanable = createAndRegisterCleanable(this, resolver);
        startWait();
    }

    public void startWait() {
        resultFuture = resolver.getResult();
        resultFuture.callback(this);
    }

    public void run(com.nabto.edge.client.swig.Status status) {
        if (status.ok()) {
            com.nabto.edge.client.swig.MdnsResult result;
            try {
                result = resultFuture.getResult();
            } catch (com.nabto.edge.client.swig.NabtoException e) {
                // this should not happen as we check the status,
                return;
            }
            listener.onChange(new MdnsResultImpl(result));
            startWait();
        }
        // else it is probably an error and we will not get any more results.
    }

    public void stop() {
        resolver.stop();
    }

    @Override
    public void close() throws Exception {
        stop();
        this.cleanable.clean();
    }

    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, com.nabto.edge.client.swig.MdnsResolver nativeHandle) {
        return CleanerService.instance().register(o, () -> nativeHandle.delete());
    }

}