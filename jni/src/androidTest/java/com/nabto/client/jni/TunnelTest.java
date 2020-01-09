package com.nabto.client.jni;

//import android.content.Context;
import android.content.res.Resources;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

import static org.junit.Assert.*;

import com.nabto.client.jni.*;

import com.nabto.client.jni.test.R;

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

    @Test
    public void loadLibrary() {
        Context context = Context.create();
    }

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
        Context context = Context.create();
        Connection connection = Helper.createConnection(context);
        connection.connect().waitForResult();
        TcpTunnel tunnel = connection.createTcpTunnel();
        tunnel.open(4242, "", 29281);

        URL url = new URL("http://127.0.0.1:4242/hello-world");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String output = readStream(in);
            assertEquals(output, "Hello World");
        } finally {
            urlConnection.disconnect();
        }

        class CallbackImpl extends FutureCallback {
            final Object syncObject = new Object();

            public Status status;
            public void run(Status status) {
                this.status = status;
                synchronized (syncObject){
                    syncObject.notify();
                }
            }

            public Status waitStatus() throws Exception {
                synchronized (syncObject){
                   syncObject.wait();
                }
                return status;
            }
        }

        FutureVoid future = connection.close();
        CallbackImpl ci = new CallbackImpl();
        future.callback(ci);
        Status closeStatus = ci.waitStatus();
        assertTrue(closeStatus.ok());
    }
}
