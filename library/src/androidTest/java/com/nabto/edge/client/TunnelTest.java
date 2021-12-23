package com.nabto.edge.client;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


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
        Connection connection = Helper.createTunnelConnection(client);
        connection.connect();
        TcpTunnel tunnel = connection.createTcpTunnel();
        tunnel.open("http", 0);
        int localPort = tunnel.getLocalPort();

        URL url = new URL("http://127.0.0.1:"+localPort+"/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        assertEquals(200, urlConnection.getResponseCode());
        urlConnection.disconnect();


        connection.close();
    }
}
