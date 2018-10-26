package com.salesforce.androidsyncengine.thread;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncThreadFactory implements ThreadFactory {
    private static AtomicInteger threadCount = new AtomicInteger();
    private final static String THREAD_NAME = "SyncThread";

    @Override
    public Thread newThread(@NonNull Runnable r) {
        return new Thread(r, THREAD_NAME + threadCount.addAndGet(1));
    }
}
