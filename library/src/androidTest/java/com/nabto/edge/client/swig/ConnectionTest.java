package com.nabto.edge.client.swig;

//import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;


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
