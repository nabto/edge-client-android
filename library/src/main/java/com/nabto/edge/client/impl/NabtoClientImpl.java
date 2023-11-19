package com.nabto.edge.client.impl;

import android.net.Network;
import java.math.BigInteger;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.nabto.edge.client.NabtoClient;
import com.nabto.edge.client.Connection;
import com.nabto.edge.client.MdnsResultListener;
import com.nabto.edge.client.MdnsScanner;

import java.util.HashMap;

public class NabtoClientImpl extends NabtoClient {
    private com.nabto.edge.client.swig.Context client;
    private final CleanerService.Cleanable cleanable;

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

    public NabtoClientImpl(Context androidContext) {
        this.client = com.nabto.edge.client.swig.Context.create();
        this.cleanable = createAndRegisterCleanable(this, this.client);

        try {
            this.client.setLogger(logger);
        } catch(Exception e) {
            Log.e("nabto", "Could not set logger", e);
        }

        wifiMonitor = new WifiMonitor(this);
        wifiMonitor.init(androidContext);

        WifiManager wifiManager = (WifiManager) androidContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.multicastLock = wifiManager.createMulticastLock("nabto_client_sdk");
        this.multicastLock.setReferenceCounted(true);

        this.wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "nabto_client_sdk");
        this.wifiLock.setReferenceCounted(true);

        this.mdnsResultListeners = new HashMap<MdnsResultListener, MdnsResultScanner>();
    }

    public void setLogLevel(String level) {
        try {
            client.setLogLevel(level);
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public String createPrivateKey() {
        return client.createPrivateKey();
    }

    void setWifiNetwork(Network network) {
        client.setAndroidWifiNetworkHandle(BigInteger.valueOf(network.getNetworkHandle()));
    }

    @Override
    public Connection createConnection() {
        return new ConnectionImpl(client.createConnection(), multicastLock, wifiLock);
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
        MdnsResultScanner scanner = new MdnsResultScanner(client, listener, subtype);
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
        return new MdnsScannerImpl(client, subtype, multicastLock, wifiLock);
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
    public void close() {
        // do not cleanup multicast and wifi locks here to not mess with reference count: only
        // the deprecated mDNS functions acquire/release the locks in this class - it is up to
        // users of the deprecated API to manage these resources.
        cleanable.clean();
    }

    /// static helper to ensure no "this" is captured accidentally
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, com.nabto.edge.client.swig.Context nativeHandle) {
        return CleanerService.instance().register(o, () -> {
            try {
                nativeHandle.setLogger(null);
            } catch (Exception e) {
                Log.e("nabto", "Could not remove logger: " + e);
            }
            nativeHandle.delete();
        });
    }

}
