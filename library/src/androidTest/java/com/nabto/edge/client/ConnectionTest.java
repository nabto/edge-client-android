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
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Optional;


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
        connection.connectionClose();
    }

    @Test(expected = Test.None.class)
    public void connectRemote() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createRemoteConnection(client);

        connection.connect();
        connection.connectionClose();
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
    public void invokeCoapInConnectionEventCallback() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createRemoteConnection(client);
        Coap coap = connection.createCoap("GET", "/hello-world");
        final AtomicInteger statusCode = new AtomicInteger();
        connection.addConnectionEventsListener(
                new ConnectionEventsCallback() {
                    @Override
                    public void onEvent(int event) {
                        // do not use assertions in callback as they are implemented as exceptions
                        // that wreak havoc when escaping
                        try {
                            coap.execute();
                        } catch (Exception e) {
                            Log.i("ConnectionTest", "Exception in onEvent: " + e);
                            return;
                        }
                        statusCode.set(coap.getResponseStatusCode());
                        latch.countDown();
                    }
                }
        );
        connection.connect();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(statusCode.get(), 205);
        connection.connectionClose();
    }

    @Test(expected = Test.None.class)
    public void closeConnectionInConnectionEventCallback_CONNECTED() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createRemoteConnection(client);
        connection.addConnectionEventsListener(
                new ConnectionEventsCallback() {
                    @Override
                    public void onEvent(int event) {
                        // do not use assertions in callback as they are implemented as exceptions
                        // that wreak havoc when escaping
                        try {
                            if (event == ConnectionEventsCallback.CONNECTED) {
                                connection.connectionClose();
                                latch.countDown();
                            }
                        } catch (Exception e) {
                            Log.i("ConnectionTest", "Exception in onEvent (event=" + event + "): " + e);
                        }
                    }
                }
        );
        connection.connect();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test(expected = Test.None.class)
    public void closeConnectionInConnectionEventCallback_CLOSED() throws Exception {
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        final CountDownLatch closedLatch = new CountDownLatch(1);
        final CountDownLatch exceptionLatch = new CountDownLatch(1);
        final AtomicInteger errorCode = new AtomicInteger();
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createRemoteConnection(client);
        connection.addConnectionEventsListener(
                new ConnectionEventsCallback() {
                    @Override
                    public void onEvent(int event) {
                        // do not use assertions in callback as they are implemented as exceptions
                        // that wreak havoc when escaping
                        try {
                            if (event == ConnectionEventsCallback.CONNECTED) {
                                connectedLatch.countDown();
                            }
                            if (event == ConnectionEventsCallback.CLOSED) {
                                closedLatch.countDown();
                                connection.connectionClose();
                            }
                        } catch (Exception e) {
                            Log.i("ConnectionTest", "Exception in onEvent (event=" + event + "): " + e);
                            exceptionLatch.countDown();
                            if (e instanceof NabtoRuntimeException) {
                                int code = ((NabtoRuntimeException)e).getErrorCode().getErrorCode();
                                errorCode.set(code);
                            }
                        }
                    }
                }
        );
        connection.connect();
        assertTrue(connectedLatch.await(5, TimeUnit.SECONDS));
        connection.connectionClose();
        assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
        assertTrue(exceptionLatch.await(5, TimeUnit.SECONDS));
        assertEquals(ErrorCodes.STOPPED, errorCode.get());
    }

    // @Test(expected = Test.None.class)
    // public void gcConnectCallback() throws Exception {
    //     NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
    //     Connection connection = Helper.createConnection(client);
    //     JSONObject options = new JSONObject();
    //     options.put("DeviceId", "unknown");
    //     connection.updateOptions(options.toString());

    //         connection.connectCallback(new NabtoCallback<Void>(){ public void run(int errorCode, Optional<Void> notUsed) {
    //             Log.i("NABTO", "this is not called");
    //         } });
    //     // The callback is not used, the gc will gc it.
    //     for (int i = 0; i < 100; i++) {
    //         Runtime.getRuntime().gc();
    //         Thread.sleep(30);
    //     }
    //     // the connect should fail at some point and the application should not crash.
    // }
};
