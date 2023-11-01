package com.nabto.edge.client.impl;

import android.util.Log;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implement a facility similar to Java 9's java.lang.ref.Cleaner, but using only Java 8 language
 * features. Kudos to https://stackoverflow.com/a/47830289.
 *
 * Whenever a Nabto Edge Client SDK native resource is allocated, it is registered with the Cleaner
 * singleton. If the instance is not explicitly released by user code (e.g. through AutoCloseable),
 * the Cleaner releases it when the resource is orphaned.
 */
class CleanerService {

    /**
     * Instances registered with the Cleaner implements this interface to be invoked when orphaned.
     */
    interface Cleanable {
        /// invoked when instance is orphaned
        void clean();
    }

    private static final CleanerService instance = new CleanerService();

    /**
     * Start the CleanerService daemon when the class is loaded. Never stop it - it runs as a
     * daemon meaning it will not prevent the JVM from exiting.
     */
    static {
        instance.startDaemon();
    }

    private Thread thread;

    /**
     * Access the CleanerService instance.
     * @return the CleanerService instance
     */
    static CleanerService instance() {
        return instance;
    }

    /**
     * Start the Cleaner daemon that periodically cleans up registered orphaned resources. This overload
     * uses the default poll period of 1 second.
     */
    void startDaemon() {
        startDaemon(1000);
    }

    /**
     * Start the Cleaner daemon that periodically cleans up registered orphaned resources.
     */
    void startDaemon(int pollPeriodMillis) {
        if (thread != null) {
            return;
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    deleteOrphaned();
                    try {
                        Thread.sleep(pollPeriodMillis);
                    } catch (InterruptedException e) {
                        Log.d("nabto", "Cleaner interrupted, doing final cleanup");
                        deleteOrphaned();
                        return;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        Log.d("nabto", "Cleaner daemon started");
    }

    /**
     * Stop the Cleaner daemon. Only needed for tests.
     */
    void stopDaemon() {
        thread.interrupt();
        try {
            thread.join();
            thread = null;
        } catch (InterruptedException e) {
            Log.w("nabto", "Resource cleaner daemon interrupted while stopping");
        }
        Log.d("nabto", "Cleaner daemon stopped");
    }

    /**
     * Register an instance to be cleaned up when orphaned.
     * @param o The instance to be cleaned up.
     * @param r The cleanup logic specific for this instance type.
     * @return
     */
    CleanerService.Cleanable register(Object o, Runnable r) {
        CleanerService.CleanerReference c = new CleanerService.CleanerReference(Objects.requireNonNull(o), Objects.requireNonNull(r));
        Log.d("nabto", "Cleaner registering object [" + o + "] with reference [" + c + "]");
//        Log.d("nabto", "Cleaner registering object [" + o + "] with reference [" + c + "] - with stack:" + Log.getStackTraceString(new Exception()));
        phantomReferences.add(c);
        return c;
    }

    private final Set<CleanerService.CleanerReference> phantomReferences = ConcurrentHashMap.newKeySet();
    private final ReferenceQueue<Object> garbageCollectedObjectsQueue = new ReferenceQueue<>();

    final class CleanerReference extends PhantomReference<Object> implements CleanerService.Cleanable {
        private final Runnable cleaningAction;

        CleanerReference(Object referent, Runnable action) {
            super(referent, garbageCollectedObjectsQueue);
            cleaningAction = action;
        }

        public void clean() {
            if (phantomReferences.remove(this)) {
                Log.d("nabto", "Cleaner cleaning reference " + this);
                super.clear();
                cleaningAction.run();
            }
        }
    }

    void deleteOrphaned() {
//        for (PhantomReference<Object> reference : phantomReferences) {
//            Log.d("nabto", "Cleaner found a reference: " + reference + ", enqueued=" + reference.isEnqueued());
//        }
        CleanerService.CleanerReference reference;
        while ((reference = (CleanerService.CleanerReference) garbageCollectedObjectsQueue.poll()) != null) {
            reference.clean();
            if (phantomReferences.isEmpty()) {
                Log.d("nabto", "All registered Cleaner references cleaned up");
            }
        }
    }
}
