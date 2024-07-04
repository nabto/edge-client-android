package com.nabto.edge.client.manualtests;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.res.Resources;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nabto.edge.client.testdata.R;

import com.nabto.edge.client.MdnsResult;
import com.nabto.edge.client.MdnsResultListener;
import com.nabto.edge.client.NabtoClient;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


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
        client.setLogLevel("trace");
        LinkedBlockingQueue<MdnsResult> blockingQueue = new LinkedBlockingQueue<MdnsResult>();
        ResultListener rl = new ResultListener();
        client.addMdnsResultListener(rl);

        String productId = resources.getString(R.string.local_product_id);
        String deviceId = resources.getString(R.string.local_device_id);

        assertNotEquals(rl.waitForDevice(productId, deviceId), null);
    }

    @Test(expected = Test.None.class)
    public void mdnsScanSubtype() throws Exception {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());

        LinkedBlockingQueue<MdnsResult> blockingQueue = new LinkedBlockingQueue<MdnsResult>();
        ResultListener rl = new ResultListener();
        client.addMdnsResultListener(rl,"swift-test-subtype");

        String productId = resources.getString(R.string.local_product_id);
        String deviceId = resources.getString(R.string.local_device_id);
        assertNotEquals(rl.waitForDevice(productId, deviceId), null);
    }

    @Test(expected = Test.None.class)
    public void mdnsTxtItems() throws Exception {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());

        LinkedBlockingQueue<MdnsResult> blockingQueue = new LinkedBlockingQueue<MdnsResult>();
        ResultListener rl = new ResultListener();
        client.addMdnsResultListener(rl,"swift-test-subtype");

        String productId = resources.getString(R.string.local_product_id);
        String deviceId = resources.getString(R.string.local_device_id);
        MdnsResult mr = rl.waitForDevice(productId, deviceId);
        assertNotEquals(mr, null);
        Map<String,String> txtItems = mr.getTxtItems();
        assertTrue(txtItems.get("swift-txt-key").equals("swift-txt-val"));
    }
}
