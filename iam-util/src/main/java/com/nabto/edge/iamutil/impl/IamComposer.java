package com.nabto.edge.iamutil.impl;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.nabto.edge.iamutil.*;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.Coap.ContentFormat;
import com.nabto.edge.client.Connection;

public class IamComposer implements AutoCloseable {
    private Coap probe;
    private Coap coap;
    private Map<Integer, IamError> errorMap;

    public IamComposer start(Connection conn, IamPath path, String... args) {
        coap = IamImplUtil.createCoap(conn, path, args);
        probe = conn.createCoap("GET", "/iam/pairing");
        return this;
    }

    public IamComposer withPayload(Object payload) {
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, IamImplUtil.encode(payload));
        return this;
    }

    public IamComposer withMap(Object[][] initializer) {
        errorMap = Stream.of(initializer).collect(Collectors.toMap(
            data -> {
                return (Integer)data[0];
            },
            data -> {
                return (IamError)data[1];
            }));
        return this;
    }

    public IamComposer execute(boolean withProbe) {
        coap.execute();
        int status = coap.getResponseStatusCode();
        if (status == 404 && withProbe) {
            // Probe /iam/pairing to see if the device supports IAM
            probe.execute();
            int probeStatus = probe.getResponseStatusCode();
            if (probeStatus != 205 && probeStatus != 403) {
                throw new IamException(IamError.IAM_NOT_SUPPORTED);
            }
        }
        return this;
    }

    public IamComposer execute() {
        return execute(true);
    }

    public IamComposer maybeThrow() {
        int status = coap.getResponseStatusCode();
        IamError err = errorMap.getOrDefault(status, IamError.FAILED);
        if (err == IamError.FAILED) {
            throw new IamException(err, "got unexpected status code " + status);
        } else if (err != IamError.NONE) {
            throw new IamException(err);
        }
        return this;
    }

    public int getStatus() {
        return coap.getResponseStatusCode();
    }

    public <T> T decodePayload(Class<T> cls) {
        return IamImplUtil.decode(coap.getResponsePayload(), cls);
    }

    public void close() {
        coap.close();
        probe.close();
    }
}
