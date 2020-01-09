package com.nabto.client.jni;

//import android.content.Context;
import android.content.res.Resources;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

import static org.junit.Assert.*;

import com.nabto.client.jni.*;

import com.nabto.client.jni.test.R;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class StreamTest {

    @Test
    public void loadLibrary() {
        Context context = Context.create();
    }

    @Test
    public void connect() throws NabtoException {

        Context context = Context.create();
        Connection connection = Helper.createConnection(context);
        connection.connect().waitForResult();
        Stream stream = connection.createStream();
        stream.open(42).waitForResult();
        byte[] toWrite = new byte[]{42,32,44,45};
        stream.write(toWrite);
        byte[] result = stream.readAll(4).waitForResult();
        assertEquals(result.length, 4);

        //for (int i = 0; i < data.length; i++) {
        assertArrayEquals(toWrite, result); // toWrite.data()[i], data[i]);
            //}
        try {
            stream.close().waitForResult();
        } catch (Exception e) {
            // TODO this should close cleanly
        }
        connection.close().waitForResult();

    }
}
