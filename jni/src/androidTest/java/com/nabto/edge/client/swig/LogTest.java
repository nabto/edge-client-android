package com.nabto.edge.client.swig;

//import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LogTest {
    @Test
    public void loggerTest() throws NabtoException {
        Context context = Context.create();
        LoggerImpl logger = new LoggerImpl();
        context.setLogger(logger);
        context.setLogLevel("trace");
        Connection connection = context.createConnection();
        assertTrue(logger.getLogs() > 0);
    }
}
