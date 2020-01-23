package com.nabto.edge.client;

//import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class ConnectionTest {

    @Test(expected = Test.None.class)
    public void connectLocal() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createConnection(client);

        JSONObject options = new JSONObject();
        options.put("Local", true);
        options.put("Remote", false);

        connection.updateOptions(options.toString());

        connection.connect();
        connection.close();
    }

    @Test(expected = Test.None.class)
    public void connectRemote() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createConnection(client);

        JSONObject options = new JSONObject();
        options.put("Local", false);
        options.put("Remote", true);
        connection.connect();
        connection.close();
    }

    @Test(expected = Test.None.class)
    public void noSuchLocalDevice() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createConnection(client);
        JSONObject options = new JSONObject();
        options.put("Local", true);
        options.put("Remote", false);
        options.put("DeviceId", "unknown");
        connection.updateOptions(options.toString());

        try {
            connection.connect();
            fail();

        } catch (NabtoNoChannelsException e) {
            assert(e.getLocalChannelErrorCode().getErrorCode() == ErrorCodes.NOT_FOUND);
        }
    }
    @Test(expected = Test.None.class)
    public void noSuchLocalOrRemoteDevice() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createConnection(client);
        JSONObject options = new JSONObject();
        //options.put("DeviceId", "unknown");
        connection.updateOptions(options.toString());
        try {
            connection.connect();
            fail();

        } catch (NabtoNoChannelsException e) {
            assertEquals(e.getLocalChannelErrorCode().getName(), new ErrorCode(ErrorCodes.NOT_FOUND).getName());
            assertEquals(e.getRemoteChannelErrorCode().getName(), new ErrorCode(ErrorCodes.NOT_ATTACHED).getName());
        }
    }
}
