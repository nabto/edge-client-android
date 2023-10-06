package com.nabto.edge.client;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ClientTest {
    @Test
    public void nabtoVersion() {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());

        String v = client.version();

        assertTrue(v, v.matches("^\\d+\\.\\d+\\.\\d+.*$"));
    }

    @Test(expected = Test.None.class)
    public void autoCloseable() throws Exception {
        class AutoCloseableTestStub implements AutoCloseable {
            private final CountDownLatch latch;
            public AutoCloseableTestStub(CountDownLatch latch) {
                this.latch = latch;
            }
            @Override
            public void close() throws Exception {
                latch.countDown();
            }
        }

        final CountDownLatch latch = new CountDownLatch(1);
        try (AutoCloseableTestStub stub = new AutoCloseableTestStub(latch)) {
            // close should be invoked now
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(0, latch.getCount());
    }
}
