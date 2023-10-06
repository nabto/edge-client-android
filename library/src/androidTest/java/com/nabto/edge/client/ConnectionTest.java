package com.nabto.edge.client;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class ConnectionTest {

    @Test(expected = Test.None.class)
    public void connectLocal() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createLocalConnection(client);

        connection.connect();
        connection.close();
    }

    @Test(expected = Test.None.class)
    public void connectRemote() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createRemoteConnection(client);

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
        options.put("DeviceId", "unknown");
        connection.updateOptions(options.toString());
        try {
            connection.connect();
            fail();

        } catch (NabtoNoChannelsException e) {
            assertEquals(e.getLocalChannelErrorCode().getName(), new ErrorCode(ErrorCodes.NOT_FOUND).getName());
            assertEquals(e.getRemoteChannelErrorCode().getName(), new ErrorCode(ErrorCodes.UNKNOWN_DEVICE_ID).getName());
            assertEquals(e.getDirectCandidatesChannelErrorCode().getName(), new ErrorCode(ErrorCodes.NONE).getName());
        }
    }

    @Test(expected = Test.None.class)
    public void connectionEventListener() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createRemoteConnection(client);
        Coap coap = connection.createCoap("GET", "/hello-world");
        connection.addConnectionEventsListener(
                new ConnectionEventsCallback() {
                    @Override
                    public void onEvent(int event) {
                        // do not use assertions in callback as they are implemented as exceptions
                        // that wreak havoc when escaping
                        try {
                            coap.execute();
                        } catch (Exception e) {
                            Log.i("ConnectionTest", "Exception in onEvent");
                            return;
                        }
                        Log.i("ConnectionTest", "Got status " + coap.getResponseStatusCode());
                        latch.countDown();
                    }
                }
        );
        connection.connect();
        boolean success = latch.await(5, TimeUnit.SECONDS);
        assertTrue(success);
        connection.close();

    }

}
