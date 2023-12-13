package com.nabto.edge.client;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class TunnelTest {

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    @Test
    public void connect() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        try (Connection connection = Helper.createTunnelConnection(client)) {
            connection.connect();
            try (TcpTunnel tunnel = connection.createTcpTunnel()) {
                tunnel.open("http", 0);
                int localPort = tunnel.getLocalPort();

                URL url = new URL("http://127.0.0.1:" + localPort + "/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                assertEquals(200, urlConnection.getResponseCode());
                urlConnection.disconnect();
            }
        }
    }

    @Test
    public void closeWithOpenTunnel() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        client.setLogLevel("trace");
        try (Connection connection = Helper.createTunnelConnection(client)) {
            connection.connect();
            try (TcpTunnel tunnel = connection.createTcpTunnel()) {
                // trigger close of tunnel
                connection.close();
                try {
                    tunnel.open("http", 0);
                    fail("Expected exception");
                } catch (NabtoRuntimeException e) {
                    assertEquals(ErrorCodes.STOPPED, e.getErrorCode().getErrorCode());
                }
            }
        }
    }

}
