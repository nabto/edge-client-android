package com.nabto.edge.client;

//import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class StreamTest {

    @Test
    public void connect() throws NabtoException {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createConnection(client);
        connection.connect();
        Stream stream = connection.createStream();
        stream.open(42);
        byte[] toWrite = new byte[]{42,32,44,45};
        stream.write(toWrite);
        byte[] result = stream.readAll(4);
        assertEquals(result.length, 4);

        //for (int i = 0; i < data.length; i++) {
        assertArrayEquals(toWrite, result); // toWrite.data()[i], data[i]);
            //}
        try {
            stream.close();
        } catch (Exception e) {
            // TODO this should close cleanly
        }
        connection.close();

    }
}
