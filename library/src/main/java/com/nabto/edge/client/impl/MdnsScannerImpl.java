package com.nabto.edge.client.impl;

import java.util.*;
import android.net.wifi.WifiManager;

import com.nabto.edge.client.MdnsScanner;
import com.nabto.edge.client.MdnsResultListener;
import com.nabto.edge.client.NabtoClient;

public class MdnsScannerImpl extends com.nabto.edge.client.swig.FutureCallback implements MdnsScanner {
    private com.nabto.edge.client.swig.Context context;
    private com.nabto.edge.client.swig.MdnsResolver resolver;
    private com.nabto.edge.client.swig.FutureMdnsResult resultFuture;
    private WifiManager.MulticastLock multicastLock;
    private WifiManager.WifiLock wifiLock;
    private Set<MdnsResultListener> listeners;
    private String subtype;
    private boolean started = false;

    MdnsScannerImpl(com.nabto.edge.client.swig.Context context, String subtype, WifiManager.MulticastLock multicastLock, WifiManager.WifiLock wifiLock) {
        this.context = context;
        this.multicastLock = multicastLock;
        this.wifiLock = wifiLock;
        this.subtype = subtype;
        this.listeners = new HashSet<MdnsResultListener>();
    }

    @Override
    public void start() {
        if (!started)
        {
            started = true;
            multicastLock.acquire();
            wifiLock.acquire();
            resolver = context.createMdnsResolver(subtype);
        }
    }

    private void startWait() {
        resultFuture = resolver.getResult();
        resultFuture.callback(this);
    }

    @Override
    public void run(com.nabto.edge.client.swig.Status status) {
        if (status.ok()) {
            com.nabto.edge.client.swig.MdnsResult result;
            try {
                result = resultFuture.getResult();
            } catch (com.nabto.edge.client.swig.NabtoException e) {
                // this should not happen as we check the status,
                return;
            }
            for (MdnsResultListener listener : listeners)
            {
                listener.onChange(new MdnsResultImpl(result));
            }
            startWait();
        }
        // else it is probably an error and we will not get any more results.
    }

    @Override
    public void stop() {
        if (started)
        {
            started = false;
            multicastLock.release();
            wifiLock.release();
            resolver.stop();
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void addMdnsResultReceiver(MdnsResultListener receiver) {
        listeners.add(receiver);
    }

    @Override
    public void removeMdnsResultReceiver(MdnsResultListener receiver) {
        listeners.remove(receiver);
    }
}