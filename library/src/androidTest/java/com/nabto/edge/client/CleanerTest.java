package com.nabto.edge.client;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.util.Log;

import com.nabto.edge.client.impl.Cleaner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
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
        Cleaner.deleteOrphaned();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(0, latch.getCount());
    }

    /// hyper fragile test that depends on explicit gc triggering and timing - disabled by default, only useful during development
    @Test(expected = Test.None.class)
    public void daemonCleansUp() throws Exception {
        Log.i("CleanerTest", "Test daemonCleansUp starts");
        Cleaner.startDaemon(10);
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
            Cleaner.stopDaemon();
        }
        Log.i("CleanerTest", "Test daemonCleansUp ends");
    }

    /// explore concept through this tutorial: https://www.baeldung.com/java-phantom-reference
//    @Test(expected = Test.None.class)
    public void phantomReferenceExample() throws Exception {
        class LargeObjectFinalizer extends PhantomReference<Object> {

            public LargeObjectFinalizer(
                    Object referent, ReferenceQueue<? super Object> q) {
                super(referent, q);
            }

            public void finalizeResources() {
                Log.i("CleanerTest", "Clearing example resource");
            }
        }

        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        List<LargeObjectFinalizer> references = new ArrayList<>();
        List<Object> largeObjects = new ArrayList<>();

        for (int i = 0; i < 10; ++i) {
            Object largeObject = new Object();
            largeObjects.add(largeObject);
            references.add(new LargeObjectFinalizer(largeObject, referenceQueue));
        }

        largeObjects = null;
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();

        Reference<?> referenceFromQueue;
        for (PhantomReference<Object> reference : references) {
            Log.i("CleanerTest", "Reference is enqueued: " + reference.isEnqueued());
        }

        while ((referenceFromQueue = referenceQueue.poll()) != null) {
            ((LargeObjectFinalizer)referenceFromQueue).finalizeResources();
            referenceFromQueue.clear();
        }
    }


}

class SomeNabtoResourceStub implements AutoCloseable {
    private final Cleaner.Cleanable cleanable;
    private final someNabtoNativeHandle someNabtoNativeHandle;

    public SomeNabtoResourceStub(CountDownLatch latch) {
        this.someNabtoNativeHandle = new someNabtoNativeHandle(latch);
        this.cleanable = createCleanable(this, someNabtoNativeHandle);
    }

    private static Cleaner.Cleanable createCleanable(Object o, someNabtoNativeHandle nfn) {
        return Cleaner.register(o, () -> nfn.cleanUp());
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    public void doStuff() {
    }

    private static class someNabtoNativeHandle {
        private final CountDownLatch latch;
        private someNabtoNativeHandle(CountDownLatch latch) {
            this.latch = latch;
        }
        private void cleanUp() {
            latch.countDown();
        }
    }
}