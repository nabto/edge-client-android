package com.nabto.edge.client;

//import android.content.Context;

import android.content.res.Resources;
import com.nabto.edge.client.test.R;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Map;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class MdnsTest {

    private class ResultListener implements MdnsResultListener {
        BlockingQueue<MdnsResult> results;
        public ResultListener() {
            this.results = new LinkedBlockingQueue<MdnsResult>();
        }
        public void onChange(MdnsResult result) {
            if (result.getAction() == MdnsResult.Action.ADD) {
                try {
                    this.results.put(result);
                } catch (Exception e) {

                }
            }
        }
        MdnsResult waitForDevice(String productId, String deviceId) {
            for (;;) {
                try {
                    MdnsResult  mr = results.poll(5, TimeUnit.SECONDS);
                    if (productId.equals(mr.getProductId()) && deviceId.equals(mr.getDeviceId())) {
                        return mr;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }

    }

    @Test(expected = Test.None.class)
    public void mdnsScan() throws Exception {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());

        LinkedBlockingQueue<MdnsResult> blockingQueue = new LinkedBlockingQueue<MdnsResult>();
        ResultListener rl = new ResultListener();
        client.addMdnsResultListener(rl);

        String productId = resources.getString(R.string.product_id);
        String deviceId = resources.getString(R.string.device_id);

        assertNotEquals(rl.waitForDevice(productId, deviceId), null);
    }
    @Test(expected = Test.None.class)
    public void mdnsScanSubtype() throws Exception {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());

        LinkedBlockingQueue<MdnsResult> blockingQueue = new LinkedBlockingQueue<MdnsResult>();
        ResultListener rl = new ResultListener();
        client.addMdnsResultListener(rl,"testdevice");

        String productId = resources.getString(R.string.product_id);
        String deviceId = resources.getString(R.string.device_id);
        assertNotEquals(rl.waitForDevice(productId, deviceId), null);
    }

    @Test(expected = Test.None.class)
    public void mdnsTxtItems() throws Exception {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());

        LinkedBlockingQueue<MdnsResult> blockingQueue = new LinkedBlockingQueue<MdnsResult>();
        ResultListener rl = new ResultListener();
        client.addMdnsResultListener(rl,"testdevice");

        String productId = resources.getString(R.string.product_id);
        String deviceId = resources.getString(R.string.device_id);
        MdnsResult mr = rl.waitForDevice(productId, deviceId);
        assertNotEquals(mr, null);
        Map<String,String> txtItems = mr.getTxtItems();
        assertTrue(txtItems.get("foo").equals("bar"));
    }
}