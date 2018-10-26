package com.salesforce.androidsyncengine.utils;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SyncEngineTimingLogger {
    final static String TAG_SYNC_MODULE = "SYNC_MODULE";


    private final static int DEFAULT_SPLIT_SIZE = 2;
    private final static String LOG_FORMAT = "%s : %f s";

    /**
     * The Log tag to use for checking Log.isLoggable and for
     * logging the timings.
     */
    private String mTag;

    /**
     * Used to track whether Log.isLoggable was enabled at reset time.
     */
    private boolean mDisabled;

    private ConcurrentHashMap<String, List<Long>> splitTiming;

    /**
     * Create and initialize a TimingLogger object that will log using
     * the specific tag. If the Log.isLoggable is not enabled to at
     * least the Log.VERBOSE level for that tag at creation time then
     * the startSplit and endPhase call will do nothing.
     *
     * @param tag the log tag to use while logging the timings
     */
    private SyncEngineTimingLogger(String tag) {
        mTag = tag;
        splitTiming = new ConcurrentHashMap<>();
        reset();
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the tag and label that was specified previously, either via
     * the constructor or a call to reset(tag, label). If the
     * Log.isLoggable is not enabled to at least the Log.VERBOSE
     * level for that tag at creation time then the startSplit and
     * endPhase call will do nothing.
     */
    public void reset() {
//        mDisabled = !Log.isLoggable(mTag, Log.VERBOSE);
        if (mDisabled) return;
        splitTiming.clear();
    }

    public void startSplit(String splitLabel) {
        addSplit(splitLabel, 0);
    }

    public void endSplit(String splitLabel) {
        addSplit(splitLabel, 1);
    }


    private void addSplit(String splitLabel, int index) {
        if (mDisabled)
            return;

        long now;

        List<Long> stopWatch = splitTiming.get(splitLabel);
        if (stopWatch == null) {
            stopWatch = new ArrayList<>(DEFAULT_SPLIT_SIZE);
            splitTiming.put(splitLabel, stopWatch);
        }

        synchronized (stopWatch) {
            now = SystemClock.elapsedRealtime();
            stopWatch.add(now);
            if (stopWatch.size() < 2) {
                return;
            }

            long elapse = stopWatch.get(1) - stopWatch.get(0);
            logInfo(String.format(LOG_FORMAT, splitLabel, elapse / 1000.0));

            stopWatch.clear();
        }
    }

    public static SyncEngineTimingLogger getLogger() {
        return SyncEngineTimingLoggerFactory.INSTANCE;
    }

    public void addDescription(String msg) {
        if (mDisabled)
            return;

        logInfo(msg);
    }


    static class SyncEngineTimingLoggerFactory {
        private static final SyncEngineTimingLogger INSTANCE = new SyncEngineTimingLogger(TAG_SYNC_MODULE);
    }

    private void logInfo(String msg) {
        FLog.e(msg);
        Log.d(mTag, msg);
    }
}




