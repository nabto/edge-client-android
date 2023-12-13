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
import com.nabto.edge.client.ErrorCode;

import java.util.HashMap;


public class ConnectionImpl implements Connection, AutoCloseable {
    com.nabto.edge.client.swig.Connection connection;
    WifiManager.MulticastLock multicastLock;
    WifiManager.WifiLock wifiLock;

    HashMap<ConnectionEventsCallback, ConnectionEventsCallbackDecorator> connectionEventsCallbacks = new HashMap<>();

    private final CleanerService.Cleanable[] cleanables = new CleanerService.Cleanable[3];

    ConnectionImpl(com.nabto.edge.client.swig.Connection connection, WifiManager.MulticastLock multicastLock, WifiManager.WifiLock wifiLock) {
        this.connection = connection;
        this.multicastLock = multicastLock;
        this.wifiLock = wifiLock;
        this.multicastLock.acquire();
        this.wifiLock.acquire();
        this.cleanables[0] = createAndRegisterCleanable(this, connection);
        this.cleanables[1] = createAndRegisterCleanable(this, multicastLock);
        this.cleanables[2] = createAndRegisterCleanable(this, wifiLock);
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
    @Override
    public void connectionClose() {
        try {
            connection.close().waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void connectionCloseCallback(NabtoCallback callback) {
        connection.close().callback(Util.makeFutureCallback(callback));
    }

    @Override
    public void close() {
        for (CleanerService.Cleanable cleanable : cleanables) {
            cleanable.clean();
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
        connection.connect().callback(Util.makeFutureCallback(callback));
    }

    public ErrorCode getLocalChannelErrorCode() {
        return new ErrorCode(connection.getLocalChannelErrorCode());
    }

    public ErrorCode getRemoteChannelErrorCode() {
        return new ErrorCode(connection.getRemoteChannelErrorCode());
    }

    public ErrorCode getDirectCandidatesChannelErrorCode() {
        return new ErrorCode(connection.getDirectCandidatesChannelErrorCode());
    }

    public void passwordAuthenticate(String username, String password) {
        try {
            connection.passwordAuthenticate(username, password).waitForResult();
        } catch (com.nabto.edge.client.swig.NabtoException e) {
            throw new com.nabto.edge.client.NabtoRuntimeException(e);
        }
    }

    public void passwordAuthenticateCallback(String username, String password, NabtoCallback callback) {
        connection.passwordAuthenticate(username, password).callback(Util.makeFutureCallback(callback));
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

    /// static helper to ensure no "this" is captured accidentally
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, com.nabto.edge.client.swig.Connection nativeHandle) {
        return CleanerService.instance().register(o, () -> nativeHandle.delete());
    }

    /// static helper to ensure no "this" is captured accidentally
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, WifiManager.MulticastLock lock) {
        return CleanerService.instance().register(o, () -> lock.release());
    }

    /// static helper to ensure no "this" is captured accidentally
    private static CleanerService.Cleanable createAndRegisterCleanable(Object o, WifiManager.WifiLock lock) {
        return CleanerService.instance().register(o, () -> lock.release());
    }
}
