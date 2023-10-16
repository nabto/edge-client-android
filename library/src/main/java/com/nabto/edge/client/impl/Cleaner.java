package com.nabto.edge.client.impl;

import android.util.Log;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
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
public class Cleaner {

    private static Thread thread;

    /**
     * Instances registered with the Cleaner implements this interface to be invoked when orphaned.
     */
    public interface Cleanable {
        /// invoked when instance is orphaned
        void clean();
    }

    /**
     * Start the Cleaner daemon that periodically cleans up registered orphaned resources. This overload
     * uses the default poll period of 1 second.
     */
    public static void startDaemon() {
        startDaemon(1000);
    }

    /**
     * Start the Cleaner daemon that periodically cleans up registered orphaned resources.
     */
    public static void startDaemon(int pollPeriodMillis) {
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        deleteOrphaned();
                        try {
                            Thread.sleep(pollPeriodMillis);
                        } catch (InterruptedException e) {
                            deleteOrphaned();
                            return;
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            Log.d("NabtoClient", "Resource cleaner daemon started");
        }
    }

    /**
     * Stop the Cleaner daemon.
     */
    public static void stopDaemon() {
        thread.interrupt();
        try {
            thread.join();
            thread = null;
        } catch (InterruptedException e) {
            Log.w("NabtoClient", "Resource cleaner daemon interrupted while stopping");
        }
        Log.d("NabtoClient", "Resource cleaner daemon stopped");
    }

    /**
     * Regster an instance to be cleaned up when orphaned.
     * @param o The instance to be claned up.
     * @param r The cleanup logic specific for this instance type.
     * @return
     */
    public static Cleanable register(Object o, Runnable r) {
        Log.d("Cleaner", "Registering object " + o);
        CleanerReference c = new CleanerReference(Objects.requireNonNull(o), Objects.requireNonNull(r));
        phantomReferences.add(c);
        return c;
    }

    private static final Set<CleanerReference> phantomReferences = ConcurrentHashMap.newKeySet();
    private static final ReferenceQueue<Object> garbageCollectedObjectsQueue = new ReferenceQueue<>();

    static final class CleanerReference extends PhantomReference<Object> implements Cleanable {
        private final Runnable cleaningAction;

        CleanerReference(Object referent, Runnable action) {
            super(referent, garbageCollectedObjectsQueue);
            cleaningAction = action;
        }

        public void clean() {
            if (phantomReferences.remove(this)) {
                super.clear();
                cleaningAction.run();
            }
        }
    }

    public static void deleteOrphaned() {
        for (PhantomReference<Object> reference : phantomReferences) {
            Log.d("Cleaner", "found a reference, enqueued=" + reference.isEnqueued());
        }
        CleanerReference reference;
        while ((reference = (CleanerReference) garbageCollectedObjectsQueue.poll()) != null) {
            Log.d("Cleaner", "cleaning a referance");
            reference.clean();
        }
    }
};
