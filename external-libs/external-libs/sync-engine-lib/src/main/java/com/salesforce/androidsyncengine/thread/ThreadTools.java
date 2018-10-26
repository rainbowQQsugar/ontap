package com.salesforce.androidsyncengine.thread;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadTools {
    private static final String TAG = "ThreadTools";
    private ThreadPoolExecutor executor;
    private static ThreadTools instance;

    private List<Future> mFetchSensitiveDataCallback;

    public static ThreadTools getThreadTools() {
        if (instance == null) {
            synchronized (ThreadTools.class) {
                if (instance == null) {
                    instance = new ThreadTools();
                }
            }
        }
        return instance;
    }

    private ThreadTools() {
        executor = new SyncExecutorService();
    }


    public void runTaskAndLockThread(Runnable... runnables) throws ExecutionException {
        if (runnables.length <= 0) {
            throw new IllegalArgumentException("Runnable count must>0 ");
        }

        List<Future> futures = new ArrayList<>(runnables.length);
        for (int i = 0; i < runnables.length; i++) {
            futures.add(executor.submit(runnables[i], ""));
        }


        waitForResult(futures);

    }

    private void waitForResult(List<Future> futures) throws ExecutionException {
        for (int i = 0; i < futures.size(); i++) {
            try {
                Log.i(TAG, "futures:" + i + " time:" + System.currentTimeMillis());
                futures.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "e:" + e);
                i = Math.max(0, i - 1);
            }
        }
    }

    public void postFetchSensitiveDataTask(Runnable task) {

        if (mFetchSensitiveDataCallback == null) {
            mFetchSensitiveDataCallback = new ArrayList<>();
        }

        mFetchSensitiveDataCallback.add(executor.submit(task));

    }


    public void waitUntilSensitiveDataTaskDone() throws ExecutionException {
        if (mFetchSensitiveDataCallback == null || mFetchSensitiveDataCallback.isEmpty()) {
            return;
        }

        waitForResult(mFetchSensitiveDataCallback);
    }


}
