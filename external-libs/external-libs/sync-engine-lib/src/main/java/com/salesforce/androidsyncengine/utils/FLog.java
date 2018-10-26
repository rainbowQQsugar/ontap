package com.salesforce.androidsyncengine.utils;


import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import com.salesforce.androidsyncengine.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FLog {

    final static Charset UTF8 = Charset.forName("UTF-8");
    private File logFile;
    private File parentFolder;

    private HandlerThread mLoggerThread;
    private Handler mHandler;

    private FLog() {
        parentFolder = new File(Environment.getExternalStorageDirectory(), "Log");
        logFile = new File(parentFolder, "SynLog.log");

    }

    private static FLog instance;
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    static {
        if (instance == null) {
            synchronized (FLog.class) {
                instance = new FLog();
            }
        }

    }


    private synchronized void ensureRequirement() {

        if (!instance.parentFolder.exists()) {
            instance.parentFolder.mkdirs();
        }

        if (mLoggerThread != null && mLoggerThread.isAlive()) {
            return;
        }

        mLoggerThread = new HandlerThread("Flog Thread");
        mLoggerThread.start();
        mHandler = new Handler(mLoggerThread.getLooper());

    }

    private void postMessage(String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(logFile, true);
                    // fileOutputStream.
                    fileOutputStream.write(msg.getBytes(UTF8));
                    fileOutputStream.flush();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private static void log(String tag, String msg) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        instance.ensureRequirement();

        StringBuilder builder = new StringBuilder();
        final String time = DATE_TIME_FORMAT.format(System.currentTimeMillis());
        builder.append(time).append(" ");
        builder.append(Thread.currentThread().getName()).append("-");
        builder.append(Thread.currentThread().getId()).append(" ");
        builder.append(tag).append(" ").append(msg).append("\n");

        write_log_in_another_thread(builder.toString());

    }

    private static void write_log_in_another_thread(String msg) {
        instance.postMessage(msg);
    }

    public static void d(String msg) {
        log("d", msg);
    }

    public static void e(String msg) {
        log("e", msg);
    }
}
