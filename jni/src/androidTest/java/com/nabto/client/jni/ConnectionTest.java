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
public class ConnectionTest {

    @Test(expected = Test.None.class)
    public void connectLocal() throws Exception {
        Context context = Context.create();
        Connection connection = Helper.createConnection(context);
        connection.setOptions("{\"Local\": true, \"Remote\": false }");

        connection.connect().waitForResult();
        connection.close().waitForResult();
    }
    @Test(expected = Test.None.class)
    public void connectRemote() throws Exception {
        Context context = Context.create();
        Connection connection = Helper.createConnection(context);
        connection.setOptions("{\"Local\": false, \"Remote\": true }");
        connection.connect().waitForResult();
        connection.close().waitForResult();
    }
}
