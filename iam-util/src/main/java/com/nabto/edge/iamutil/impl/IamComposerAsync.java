package com.nabto.edge.iamutil.impl;

import java.util.Optional;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.nabto.edge.iamutil.*;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.Coap.ContentFormat;
import com.nabto.edge.client.Connection;
import com.nabto.edge.client.NabtoCallback;
import com.nabto.edge.client.ErrorCodes;

public class IamComposerAsync<T> {
    private Coap probe;
    private Coap coap;
    private Map<Integer, IamError> errorMap;
    private Map<Integer, T> resultMap = null;
    private Class<T> resultType = null;
    private IamCallback<T> userCallback;
    private IamPath path;

    // In a composer chain only the last element has first != null
    // So when execute() is called on the last element, it can rewind to the first
    // composer in the chain and start from there.
    private IamComposerAsync<T> first;
    private IamComposerAsync<T> next = null;

    public IamComposerAsync() {
        this(null);
    }

    public IamComposerAsync(IamComposerAsync<T> first) {
        this.first = first;
    }

    public IamComposerAsync start(Connection conn, IamPath path, String... args) {
        this.path = path;
        coap = IamImplUtil.createCoap(conn, path, args);
        probe = conn.createCoap("GET", "/iam/pairing");
        return this;
    }

    public IamComposerAsync then(Connection conn, IamPath path, String... args) {
        IamComposerAsync<T> result = new IamComposerAsync<>(first != null ? first : this);
        this.first = null;
        this.next = result;
        return result.start(conn, path, args).withUserCallback(userCallback);
    }

    public IamComposerAsync withPayload(Object payload) {
        coap.setRequestPayload(ContentFormat.APPLICATION_CBOR, IamImplUtil.encode(payload));
        return this;
    }

    public IamComposerAsync withMap(Object[][] initializer) {
        errorMap = Stream.of(initializer).collect(Collectors.toMap(
            data -> {
                return (Integer)data[0];
            },
            data -> {
                return (IamError)data[1];
            }));
        return this;
    }

    public IamComposerAsync withResultMap(Object[][] initializer) {
        resultMap = Stream.of(initializer).collect(Collectors.toMap(
            data -> {
                return (Integer)data[0];
            },
            data -> {
                return (T)data[1];
            }));
        return this;
    }

    public IamComposerAsync withResultType(Class<T> cls) {
        resultType = cls;
        return this;
    }

    public IamComposerAsync withUserCallback(IamCallback<T> cb) {
        userCallback = cb;
        return this;
    }

    private void postExecute() {
        int status = coap.getResponseStatusCode();
        IamError err = errorMap.getOrDefault(status, IamError.FAILED);
        if (err == IamError.NONE) {
            if (next != null)
            {
                next.execute();
            } else if (resultType != null) {
                T decodedPayload = IamImplUtil.decode(coap.getResponsePayload(), resultType);
                userCallback.run(err, Optional.of(decodedPayload));
            } else if (resultMap != null) {
                userCallback.run(err, Optional.of(resultMap.get(status)));
            } else {
                userCallback.run(err, Optional.empty());
            }
        } else {
            userCallback.run(err, Optional.empty());
        }

        if (next == null)
        {
            coap.close();
            probe.close();
        }
    }

    public IamComposerAsync execute(boolean withProbe) {
        if (first != null) {
            IamComposerAsync<T> temp = first;
            first = null;
            temp.execute();
            return this;
        }

        NabtoCallback<Void> coapCallback = (errorCode, arg) -> {
            if (errorCode != ErrorCodes.OK) {
                userCallback.run(IamError.FAILED, Optional.empty());
                return;
            }

            int status = coap.getResponseStatusCode();

            if (status == 404 && withProbe) {
                // Probe /iam/pairing to see if the device supports IAM
                // @TODO: Unsure if making this a callback is a good idea... further investigation needed
                NabtoCallback probeCallback = (probeCode, probeArg) -> {
                    int probeStatus = probe.getResponseStatusCode();
                    if (probeStatus != 205 && probeStatus != 403) {
                        // Iam is not supported by the device
                        userCallback.run(IamError.IAM_NOT_SUPPORTED, Optional.empty());
                        coap.close();
                        probe.close();
                    } else {
                        // Iam is supported by the device, but the coap command that was executed was invalid
                        // Let the caller handle it
                        postExecute();
                    }
                };
                probe.executeCallback(probeCallback);
            } else {
                postExecute();
            }
        };

        coap.executeCallback(coapCallback);
        return this;
    }

    public IamComposerAsync execute() {
        return execute(true);
    }

}
