package com.nabto.edge.client.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nabto.edge.client.test.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.content.res.Resources;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SimpleTest {
    Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();
    @Test
    public void test() {
        resources.getString(R.string.product_id);
        assertTrue(true);

    }
}
