package com.nabto.edge.client.impl;

import android.net.wifi.WifiManager;

import com.nabto.edge.client.Connection;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.ConnectionEventsCallback;
import com.nabto.edge.client.Stream;

import java.util.HashMap;
import java.util.HashSet;


public class ConnectionImpl implements Connection {

    com.nabto.client.jni.Connection connection;
    WifiManager.MulticastLock multicastLock;
    WifiManager.WifiLock wifiLock;

    HashMap<ConnectionEventsCallback, ConnectionEventsCallbackDecorator> connectionEventsCallbacks = new HashMap<ConnectionEventsCallback, ConnectionEventsCallbackDecorator>();

    ConnectionImpl(com.nabto.client.jni.Connection connection, WifiManager.MulticastLock multicastLock, WifiManager.WifiLock wifiLock) {
        this.connection = connection;
        this.multicastLock = multicastLock;
        this.wifiLock = wifiLock;
        this.multicastLock.acquire();
        this.wifiLock.acquire();
    }

    public void updateOptions(String json) {
        try {
            connection.setOptions(json);
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public String getOptions() {
        try {
            return connection.getOptions();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public String getDeviceFingerprintHex() {
        try {
            return connection.getDeviceFingerprintHex();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }

    }

    public String getClientFingerprintHex() {
        try {
            return connection.getClientFingerprintHex();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    /**
     * Blocking close
     */
    public void close() {
        try {
            connection.close().waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    /**
     * Blocking connect
     */
    public void connect() {
        try {
            connection.connect().waitForResult();
        } catch (com.nabto.client.jni.NabtoException e) {
            throw new com.nabto.edge.client.NabtoException(e);
        }
    }

    public Coap createCoap(String method, String path) {
        return new CoapImpl(connection.createCoap(method, path));
    }

    public Stream createStream() {
        return new StreamImpl(connection.createStream());
    }

    @Override
    public void finalize() {
        this.multicastLock.release();
        this.wifiLock.release();
    }

    @Override
    public void addConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback)
    {
        ConnectionEventsCallbackDecorator decorator = new ConnectionEventsCallbackDecorator(connectionEventsCallback);
        connectionEventsCallbacks.put(connectionEventsCallback, decorator);
        this.connection.addEventsListener(decorator);
    }

    @Override
    public void removeConnectionEventsListener(ConnectionEventsCallback connectionEventsCallback)
    {
        ConnectionEventsCallbackDecorator decorator = connectionEventsCallbacks.remove(connectionEventsCallback);
        this.connection.removeEventsListener(decorator);
    }

}
