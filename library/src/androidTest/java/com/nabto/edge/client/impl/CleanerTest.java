package com.nabto.edge.client.impl;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CleanerTest {

    @Test(expected = Test.None.class)
    public void reproForceStop() throws Exception {
        assertTrue(true);
    }

    @Test(expected = Test.None.class)
    public void deterministicCleanupThroughAutoClosable() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try (SomeNabtoResourceStub stub = new SomeNabtoResourceStub(latch)) {
            stub.doStuff();
            // cleanup should be invoked immediately when reaching end of this scope
        }
        assertEquals(0, latch.getCount());
    }

    /// hyper fragile test that depends on explicit gc triggering and timing - disabled by default, only useful during development
    @Test(expected = Test.None.class)
    public void phantomReferenceCleanup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        {
            SomeNabtoResourceStub stub = new SomeNabtoResourceStub(latch);
            // cleanup should be invoked when stub becomes a phantom reference
            stub.doStuff();
        }
        Runtime.getRuntime().gc();
        // some time is needed from gc to the orphaned references to be queued
        Thread.sleep(100);
        CleanerService.instance().deleteOrphaned();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(0, latch.getCount());
    }

    /// hyper fragile test that depends on explicit gc triggering and timing - disabled by default, only useful during development
    @Test(expected = Test.None.class)
    public void daemonCleansUp() throws Exception {
        Log.i("CleanerTest", "Test daemonCleansUp starts");
        CleanerService.instance().startDaemon(10);
        try {
            CountDownLatch fooLatch = new CountDownLatch(1);
            CountDownLatch barLatch = new CountDownLatch(1);
            {
                SomeNabtoResourceStub foo = new SomeNabtoResourceStub(fooLatch);
                SomeNabtoResourceStub bar = new SomeNabtoResourceStub(barLatch);
                foo.doStuff();
                bar.doStuff();
            }
            Runtime.getRuntime().gc();
            Thread.sleep(100);
            Runtime.getRuntime().gc();
            Thread.sleep(100);
            assertTrue(fooLatch.await(1, TimeUnit.SECONDS));
            assertTrue(barLatch.await(1, TimeUnit.SECONDS));
            assertEquals(0, fooLatch.getCount());
            assertEquals(0, barLatch.getCount());
        } finally {
            CleanerService.instance().stopDaemon();
        }
        Log.i("CleanerTest", "Test daemonCleansUp ends");
    }
}

class SomeNabtoResourceStub implements AutoCloseable {
    private final CleanerService.Cleanable cleanable;
    private final SomeNabtoNativeHandle someNabtoNativeHandle;

    public SomeNabtoResourceStub(CountDownLatch latch) {
        this.someNabtoNativeHandle = new SomeNabtoNativeHandle(latch);
        this.cleanable = createCleanable(this, someNabtoNativeHandle);
    }

    /**
     * "Care must be taken not to capture the this instance, that’s why the creation has been
     * moved into a static method in the example above. Without a this in scope, it can’t be
     * captured by accident." (from https://stackoverflow.com/questions/46144524/delete-native-peer-with-general-phantomreference-class/47830289#47830289)
     */
    private static CleanerService.Cleanable createCleanable(Object o, SomeNabtoNativeHandle nativeHandle) {
        return CleanerService.instance().register(o, () -> nativeHandle.cleanUp());
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    public void doStuff() {
    }

    private static class SomeNabtoNativeHandle {
        private final CountDownLatch latch;
        private SomeNabtoNativeHandle(CountDownLatch latch) {
            this.latch = latch;
        }
        private void cleanUp() {
            latch.countDown();
        }
    }
}