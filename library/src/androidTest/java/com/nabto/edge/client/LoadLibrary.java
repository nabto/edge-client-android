package com.nabto.edge.client;

import android.content.Context;

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
public class LoadLibrary {
    @Test
    public void loadLibrary() {
        Context c = InstrumentationRegistry.getInstrumentation().getContext();
        NabtoClient client = NabtoClient.create(c);
        assert(true);
    }
}