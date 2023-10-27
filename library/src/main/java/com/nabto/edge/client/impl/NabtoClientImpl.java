package com.nabto.edge.client.impl;

import android.net.Network;
import java.math.BigInteger;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.nabto.edge.client.NabtoClient;
import com.nabto.edge.client.Connection;
import com.nabto.edge.client.MdnsResultListener;
import com.nabto.edge.client.MdnsScanner;

import java.util.HashMap;

public class NabtoClientImpl extends NabtoClient {
    // this specific native resource is not registered with the CleanerService as the service's
    // life cycle itself is controlled from within this class
    private com.nabto.edge.client.swig.Context context = com.nabto.edge.client.swig.Context.create();
    private Logger logger = new Logger();
    private WifiMonitor wifiMonitor;
    /**
     * The multicast lock is needed else the application is not allowed to receive
     * multicast packets.
     */
    private WifiManager.MulticastLock multicastLock;

    /**
     * The wifi lock is needed else the application is not guaranteed to have wifi access in
     * some scenarios
     */
    private WifiManager.WifiLock wifiLock;

    private HashMap<MdnsResultListener, MdnsResultScanner> mdnsResultListeners;

    public NabtoClientImpl(Context context) {
        try {
            this.context.setLogger(logger);
        } catch(Exception e) {
        }

        wifiMonitor = new WifiMonitor(this);
        wifiMonitor.init(context);

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.multicastLock = wifiManager.createMulticastLock("nabto_client_sdk");
        this.multicastLock.setReferenceCounted(true);

        this.wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "nabto_client_sdk");
        this.wifiLock.setReferenceCounted(true);

        this.mdnsResultListeners = new HashMap<MdnsResultListener, MdnsResultScanner>();
    }

    public void setLogLevel(String level) {
        try {
            context.setLogLevel(level);
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public String createPrivateKey() {
        return context.createPrivateKey();
    }

    void setWifiNetwork(Network network) {
        context.setAndroidWifiNetworkHandle(BigInteger.valueOf(network.getNetworkHandle()));
    }

    @Override
    public Connection createConnection() {
        return new ConnectionImpl(context.createConnection(), multicastLock, wifiLock);
    }

    @Deprecated
    @Override
    public void addMdnsResultListener(MdnsResultListener listener)
    {
        addMdnsResultListener(listener, "");
    }

    @Deprecated
    @Override
    public void addMdnsResultListener(MdnsResultListener listener, String subtype)
    {
        MdnsResultScanner scanner = new MdnsResultScanner(context, listener, subtype);
        multicastLock.acquire();
        wifiLock.acquire();
        mdnsResultListeners.put(listener, scanner);
    }

    @Deprecated
    @Override
    public void removeMdnsResultListener(MdnsResultListener listener)
    {
        MdnsResultScanner scanner = mdnsResultListeners.get(listener);
        if (scanner != null) {
            scanner.stop();
        }
        multicastLock.release();
        wifiLock.release();
        mdnsResultListeners.remove(listener);
    }

    public MdnsScanner createMdnsScanner(String subtype)
    {
        return new MdnsScannerImpl(context, subtype, multicastLock, wifiLock);
    }

    public MdnsScanner createMdnsScanner()
    {
        return createMdnsScanner("");
    }

    @Override
    public String version() {
        return com.nabto.edge.client.swig.Context.version();
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    @Override
    public void stop() {
    }
}
