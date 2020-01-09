package com.nabto.client.jni;

//import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import android.util.Log;

import static org.junit.Assert.*;

import com.nabto.client.jni.LoggerImpl;

import com.nabto.client.jni.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DummyTest {
    @Test
    public void dummy() {
        int x = 2;
        int y = 4;
        assertEquals(x+y, 6);
    }
}
