package com.nabto.client.jni;

//import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.nabto.client.jni.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExceptionTest {

    @Test
    public void exceptionTest() {
        Context context = Context.create();
        try {
            context.setLogLevel("invalid");
            // never here
            assertFalse(true);
        } catch (NabtoException ne) {
            assertEquals(ne.getMessage(), "Bad argument");
        }
    }
}
