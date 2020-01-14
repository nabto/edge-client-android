package com.nabto.edge.client;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Test
    public void libraryVersion() {
        String v = com.nabto.edge.client.BuildConfig.VERSION_NAME;
        assertTrue(v, v.matches("^\\d+\\.\\d+\\.\\d+.*$"));
    }

}
