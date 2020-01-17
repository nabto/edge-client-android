package com.nabto.edge.client.impl;

import android.net.wifi.WifiManager;

import com.nabto.edge.client.Connection;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.ConnectionEventsCallback;
import com.nabto.edge.client.ErrorCodes;
import com.nabto.edge.client.NabtoConnectFailedException;
import com.nabto.edge.client.Stream;
import com.nabto.edge.client.TcpTunnel;

import org.json.JSONObject;

import java.util.HashMap;


public class ConnectionImpl implements Connection {

    com.nabto.edge.client.swig.Connection connection;
    WifiManager.MulticastLock multicastLock;
    WifiManager.WifiLock wifiLock;

    HashMap<ConnectionEventsCallback, ConnectionEventsCallbackDecorator> connectionEventsCallbacks = new HashMap<ConnectionEventsCallback, ConnectionEventsCallbackDecorator>();

    ConnectionImpl(com.nabto.edge.client.swig.Connection connection, WifiManager.MulticastLock multicastLock, WifiManager.WifiLock wifiLock) {
        this.connection = connection;
        this.multicastLock = multicastLock;
        this.wifiLock = wifiLock;
        this.multicastLock.acquire();
        this.wifiLock.acquire();
    }

    public void updateOptions(String json) {
        try {
            connection.setOptions(json);
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public String getOptions() {
        try {
            return connection.getOptions();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public String getDeviceFingerprintHex() {
        try {
            return connection.getDeviceFingerprintHex();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }

    }

    public String getClientFingerprintHex() {
        try {
            return connection.getClientFingerprintHex();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    /**
     * Blocking close
     */
    public void close() {
        try {
            connection.close().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    /**
     * Blocking connect
     */
    public void connect() {
        try {
            connection.connect().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            if (e.status().getErrorCode() == ErrorCodes.NO_CHANNELS) {
                int mdnsEc = connection.getMdnsChannelErrorCode();
                int udpRelayEc = connection.getUdpRelayChannelErrorCode();

                throw new NabtoConnectFailedException(mdnsEc, udpRelayEc);

            } else {
                throw new com.nabto.edge.client.NabtoRuntimeException(e);
            }
        }
    }

    public Coap createCoap(String method, String path) {
        return new CoapImpl(connection.createCoap(method, path));
    }

    public Stream createStream() {
        return new StreamImpl(connection.createStream());
    }

    public TcpTunnel createTcpTunnel() {
        return new TcpTunnelImpl(connection.createTcpTunnel());
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
