package com.abinbev.dsa.utils;

import android.os.Looper;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AppScheduler {

    public static Scheduler background() {
        return backgroundIfNeeded();
    }

    public static Scheduler main() {
        return AndroidSchedulers.mainThread();
    }

    /**
     * @return a background scheduler if we are on the Android main thread. Otherwise the current thread.
     */
    private static Scheduler backgroundIfNeeded() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return Schedulers.io();
        } else {
            return Schedulers.immediate();
        }
    }
}
