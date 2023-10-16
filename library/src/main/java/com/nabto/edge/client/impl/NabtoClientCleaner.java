package com.nabto.edge.client.impl;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class NabtoClientCleaner {
    private static final NabtoClientCleaner cleaner = new NabtoClientCleaner();

    public static NabtoClientCleaner getCleaner() {
        return cleaner;
    }
}
