package com.nabto.edge.client;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class StreamTest {

    @Test
    public void echo() {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createStreamConnection(client);
        connection.connect();
        Stream stream = connection.createStream();
        stream.open(42);
        byte[] toWrite = new byte[]{42,32,44,45};
        stream.write(toWrite);
        try {
            byte[] result = stream.readAll(4);
            assertEquals(result.length, 4);
            assertArrayEquals(toWrite, result); // toWrite.data()[i], data[i]);
        } catch (NabtoEOFException e) {
            assert(false);
        }

        stream.streamClose();
        connection.connectionClose();
    }

    @Test
    public void echoUntilEOF() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createStreamConnection(client);
        connection.connect();
        Stream stream = connection.createStream();
        stream.open(42);
        byte[] toWrite = new byte[]{42,32,44,45};
        stream.write(toWrite);
        stream.streamClose();
        try {
            byte[] result = stream.readAll(4);
            assertEquals(result.length, 4);
        } catch (NabtoEOFException e) {
            throw e;
        }

        boolean eof = false;
        try {
            byte[] result = stream.readAll(4);
        } catch (NabtoEOFException e) {
            assertTrue(e.getErrorCode().getErrorCode() == ErrorCodes.END_OF_FILE);
            eof = true;
        } finally {
            assertTrue(eof);
        }

        stream.streamClose();
        connection.connectionClose();

    }
    @Test
    public void echoUntilEOFCallbacks() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        client.setLogLevel("trace");
        Connection connection = Helper.createStreamConnection(client);
        connection.connect();
        Stream stream = connection.createStream();
        stream.open(42);
        byte[] toWrite = new byte[]{42,32,44,45};
        stream.write(toWrite);
        stream.streamClose();

        stream.readAllCallback(4, new NabtoCallback<byte[]>() {
            @Override
            public void run(int errorCode, Optional<byte[]> arg) {
                assertEquals(errorCode, ErrorCodes.OK);
                assertTrue(arg.isPresent());
                assertEquals(arg.get().length, 4);
                stream.readAllCallback(4, new NabtoCallback<byte[]>() {
                    @Override
                    public void run(int errorCode, Optional<byte[]> arg) {
                        assertEquals(errorCode, ErrorCodes.END_OF_FILE);
                        latch.countDown();
                    }
                });
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        stream.streamClose();
        connection.connectionClose();

    }

    @Test
    public void echoWithCleanup() {
        try (NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext())) {
            try (Connection connection = Helper.createStreamConnection(client)) {
                connection.connect();
                Stream stream = connection.createStream();
                stream.open(42);
                byte[] toWrite = new byte[]{42, 32, 44, 45};
                stream.write(toWrite);
                try {
                    byte[] result = stream.readAll(4);
                    assertEquals(result.length, 4);
                    assertArrayEquals(toWrite, result); // toWrite.data()[i], data[i]);
                } catch (NabtoEOFException e) {
                    assert (false);
                }

                stream.streamClose();
                connection.connectionClose();
            }
        }
    }

}
