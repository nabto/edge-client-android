package com.nabto.edge.client.impl;

import android.net.wifi.WifiManager;

import com.nabto.edge.client.Connection;
import com.nabto.edge.client.Coap;
import com.nabto.edge.client.ConnectionEventsCallback;
import com.nabto.edge.client.ErrorCodes;
import com.nabto.edge.client.NabtoNoChannelsException;
import com.nabto.edge.client.Stream;
import com.nabto.edge.client.TcpTunnel;
import com.nabto.edge.client.NabtoCallback;

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
    
    public String getDeviceFingerprint() {
        try {
            return connection.getDeviceFingerprint();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }

    }

    public String getClientFingerprint() {
        try {
            return connection.getClientFingerprint();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public Type getType() {
        try {
            com.nabto.edge.client.swig.Connection.Type type = connection.getType();
            if (type == com.nabto.edge.client.swig.Connection.Type.RELAY) {
                return Type.RELAY;
            }
            if (type == com.nabto.edge.client.swig.Connection.Type.DIRECT) {
                return Type.DIRECT;
            }
            throw new com.nabto.edge.client.NabtoRuntimeException(new com.nabto.edge.client.swig.NabtoException(com.nabto.edge.client.swig.Status.getUNKNOWN()));
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    /**
     * Enable the direct candidates feature for the connection.
     */
    public void enableDirectCandidates() {
        try {
            connection.enableDirectCandidates();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    /**
     * Add a diect candidate.
     */
    public void addDirectCandidate(String host, int port) {
        try {
            connection.addDirectCandidate(host, port);
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    /**
     * Mark the end of direct candidates,
     */
    public void endOfDirectCandidates() {
        try {
            connection.endOfDirectCandidates();
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
                int localEc = connection.getLocalChannelErrorCode();
                int remoteEc = connection.getRemoteChannelErrorCode();
                int directCandidatesEc = connection.getDirectCandidatesChannelErrorCode();

                throw new NabtoNoChannelsException(localEc, remoteEc, directCandidatesEc);

            } else {
                throw new com.nabto.edge.client.NabtoRuntimeException(e);
            }
        }
    }

    public void connectCallback(NabtoCallback callback) {
        com.nabto.edge.client.swig.FutureCallback cb = new com.nabto.edge.client.swig.FutureCallback() {
            public void run(com.nabto.edge.client.swig.Status status) {
                callback.run(status.getErrorCode(), null);
            }
        };
        connection.connect().callback(cb);
    }

    /**
     * Blocking close
     */
    public void passwordAuthenticate(String username, String password) {
        try {
            connection.passwordAuthenticate(username, password).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
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
