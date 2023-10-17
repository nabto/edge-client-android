package com.nabto.edge.client.impl;

import android.util.Log;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class CleanerService {

    /**
     * Instances registered with the Cleaner implements this interface to be invoked when orphaned.
     */
    interface Cleanable {
        /// invoked when instance is orphaned
        void clean();
    }

    private static final CleanerService instance = new CleanerService();
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
    void stopDaemon() {
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
    CleanerService.Cleanable register(Object o, Runnable r) {
        Log.d("Cleaner", "Registering object " + o);
        CleanerService.CleanerReference c = new CleanerService.CleanerReference(Objects.requireNonNull(o), Objects.requireNonNull(r));
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
                super.clear();
                cleaningAction.run();
            }
        }
    }

    void deleteOrphaned() {
        for (PhantomReference<Object> reference : phantomReferences) {
            Log.d("Cleaner", "found a reference, enqueued=" + reference.isEnqueued());
        }
        CleanerService.CleanerReference reference;
        while ((reference = (CleanerService.CleanerReference) garbageCollectedObjectsQueue.poll()) != null) {
            Log.d("Cleaner", "cleaning a referance");
            reference.clean();
        }
    }
}
