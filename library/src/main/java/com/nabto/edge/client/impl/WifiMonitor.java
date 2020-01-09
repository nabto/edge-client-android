package com.nabto.edge.client.impl;

import android.net.Network;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;

import android.content.Context;

import android.util.Log;

/**
 * Monitor the state of the wifi and inform the nabto core if the network is changed.
 */
public class WifiMonitor {

    private final NabtoClientImpl client;

    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    WifiMonitor(NabtoClientImpl client) {
        this.client = client;
    }

    void init(Context context) {

        NetworkRequest request = (new NetworkRequest.Builder()).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();

        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Log.d("nabto", "registering WiFi network callback");
        this.networkCallback = new ConnectivityManager.NetworkCallback() {

                @Override
                public void onAvailable(Network network) {
                    Log.d("nabto", "Found WiFi network " + network + " handle " + network.getNetworkHandle());
                    client.setWifiNetwork(network);
                }
            };
        this.connectivityManager.registerNetworkCallback(request, this.networkCallback);
    }

    protected void finalize() {
        this.connectivityManager.unregisterNetworkCallback(this.networkCallback);
    }

};
