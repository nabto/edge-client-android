package com.nabto.edge.client;

import com.nabto.edge.client.NabtoClient;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import androidx.test.platform.app.InstrumentationRegistry;
import android.content.Context;

@RunWith(AndroidJUnit4.class)
public class StartStopTest {

    @Test
    public void startStop() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        NabtoClient client = NabtoClient.create(context);
    }
}
