package com.nabto.edge.client.swig;

//import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CoapTest {
    @Test
    public void loadLibrary() {
        Context context = Context.create();
    }

    @Test
    public void connect() throws NabtoException {
        Context context = Context.create();
        Connection connection = Helper.createConnection(context);
        connection.connect().waitForResult();
        Coap coap = connection.createCoap("GET", "/test/get");
        coap.execute().waitForResult();

        int statusCode = coap.getResponseStatusCode();
        int contentFormat = coap.getResponseContentFormat();
        byte[] payload = coap.getResponsePayload();

        assertEquals(statusCode, 205); // 2.05 == content
        assertEquals(contentFormat, 0); // 0 == utf8

        connection.close().waitForResult();
    }
}
