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

    @Test
    public void multipleTunnels() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection1 = Helper.createTunnelConnection(client);
        Connection connection2 = Helper.createTunnelConnection(client);
        connection1.connect();
        connection2.connect();
        TcpTunnel tunnel1 = connection1.createTcpTunnel();
        TcpTunnel tunnel2 = connection2.createTcpTunnel();
        tunnel1.open("http", 0);
        tunnel2.open("http", 0);
        int localPort1 = tunnel1.getLocalPort();
        int localPort2 = tunnel2.getLocalPort();

        URL url1 = new URL("http://127.0.0.1:"+localPort1+"/");
        URL url2 = new URL("http://127.0.0.1:"+localPort2+"/");
        HttpURLConnection urlConnection1 = (HttpURLConnection) url1.openConnection();
        HttpURLConnection urlConnection2 = (HttpURLConnection) url2.openConnection();
        assertEquals(200, urlConnection1.getResponseCode());
        assertEquals(200, urlConnection2.getResponseCode());
        urlConnection1.disconnect();
        urlConnection2.disconnect();


        connection1.close();
        connection2.close();
    }

}
