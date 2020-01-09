package com.nabto.edge.client;

//import android.content.Context;
import android.content.res.Resources;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

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
public class ConnectionTest {

    @Test(expected = Test.None.class)
    public void connectLocal() throws Exception {

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        client.setLogLevel("trace");
        Connection connection = Helper.createConnection(client);
        connection.setOptions("{\"Local\": true, \"Remote\": false }");

        connection.connect();
        connection.close();
    }
}
