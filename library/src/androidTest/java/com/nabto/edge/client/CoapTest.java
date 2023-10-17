package com.nabto.edge.client;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CoapTest {
    @Test
    public void invokeCoapHello() {
        try {
            NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
            client.setLogLevel("trace");

            Connection connection = Helper.createRemoteConnection(client);
            connection.connect();
            Coap coap = connection.createCoap("GET", "/hello-world");
            coap.execute();

            int statusCode = coap.getResponseStatusCode();
            int contentFormat = coap.getResponseContentFormat();
            byte[] payload = coap.getResponsePayload();

            assertEquals(statusCode, 205); // 2.05 == content
            assertEquals(contentFormat, 0); // 0 == utf8

            connection.close();
        } catch (Exception e) {
            if (e instanceof NabtoNoChannelsException) {
                fail("NabtoNoChannelsException - local error: " + ((NabtoNoChannelsException) e).getLocalChannelErrorCode().getDescription() +
                        "; remote error: " + ((NabtoNoChannelsException) e).getRemoteChannelErrorCode().getDescription());
            } else {
                throw e;
            }
        }
    }


    @Test
    public void invokeCoapHelloRepeat3Times() {
        try {
            NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
            client.setLogLevel("trace");

            Connection connection = Helper.createRemoteConnection(client);
            connection.connect();
            for (int i = 0; i < 3; i++) {
                Coap coap = connection.createCoap("GET", "/hello-world");
                coap.execute();

                int statusCode = coap.getResponseStatusCode();
                int contentFormat = coap.getResponseContentFormat();
                byte[] payload = coap.getResponsePayload();

                assertEquals(statusCode, 205); // 2.05 == content
                assertEquals(contentFormat, 0); // 0 == utf8
            }
            connection.close();
        } catch (Exception e) {
            if (e instanceof NabtoNoChannelsException) {
                fail("NabtoNoChannelsException - local error: " + ((NabtoNoChannelsException) e).getLocalChannelErrorCode().getDescription() +
                        "; remote error: " + ((NabtoNoChannelsException) e).getRemoteChannelErrorCode().getDescription());
            } else {
                throw e;
            }
        }
    }

    @Test
    public void invokeCoapHelloRepeatInCallback() throws Exception {
        try {
            NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
            client.setLogLevel("trace");
            Connection connection = Helper.createRemoteConnection(client);
            connection.connect();
            CountDownLatch latch = new CountDownLatch(3);
            BlockingQueue<CoapResult> queue = new LinkedBlockingQueue<>();
            for (int i = 0; i < 3; i++) {
                Coap coap = connection.createCoap("GET", "/hello-world");
                coap.executeCallback((errorCode, arg) -> {
                    latch.countDown();
                    int statusCode = coap.getResponseStatusCode();
                    int contentFormat = coap.getResponseContentFormat();
                    byte[] payload = coap.getResponsePayload();
                    queue.add(new CoapResult(statusCode, contentFormat, payload));
                });
            }
            latch.await(1000, TimeUnit.MILLISECONDS);
            connection.close();
            while (!queue.isEmpty()) {
                CoapResult result = queue.take();
                assertEquals(result.getStatusCode(), 205);
                assertEquals(result.getContentFormat(), 0);
            }
        } catch (Exception e) {
            if (e instanceof NabtoNoChannelsException) {
                fail("NabtoNoChannelsException - local error: " + ((NabtoNoChannelsException) e).getLocalChannelErrorCode().getDescription() +
                        "; remote error: " + ((NabtoNoChannelsException) e).getRemoteChannelErrorCode().getDescription());
            } else {
                throw e;
            }
        }
    }

    static class CoapResult {
        private int statusCode;
        private int contentFormat;
        private byte[] payload;

        public CoapResult(int statusCode, int contentFormat, byte[] payload) {
            this.statusCode = statusCode;
            this.contentFormat = contentFormat;
            this.payload = payload;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public int getContentFormat() {
            return contentFormat;
        }

        public byte[] getPayload() {
            return payload;
        }
    }

}
