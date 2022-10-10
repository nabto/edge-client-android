package com.nabto.edge.client.impl;

import java.util.ArrayList;
import com.nabto.edge.client.*;

public class MdnsScannerImpl implements MdnsScanner {
    private NabtoClient client;
    private String subtype;
    private boolean started = false;

    private ArrayList<MdnsResultListener> childListeners = new ArrayList<>();
    private MdnsResultListener mainListener = result -> { 
        for (MdnsResultListener listener: childListeners) {
            listener.onChange(result);
        }
    };

    MdnsScannerImpl(NabtoClient client, String subtype) {
        this.client = client;
        this.subtype = subtype;
    }

    @Override
    public void start() {
        started = true;
        client.addMdnsResultListener(mainListener, subtype);
    }

    @Override
    public void stop() {
        started = false;
        client.removeMdnsResultListener(mainListener);
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void addMdnsResultListener(MdnsResultListener listener) {
        childListeners.add(listener);
    }

    @Override
    public void removeMdnsResultListener(MdnsResultListener listener) {
        childListeners.remove(listener);
    }
}
