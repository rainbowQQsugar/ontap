package com.salesforce.androidsyncengine.thread;

import android.os.Build;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SyncExecutorService extends ThreadPoolExecutor {
    private static final int DEFAULT_THREAD_COUNT = 1;
    private static int DEFAULT_MAX_THREAD_COUNT = 6;

    static {
        DEFAULT_MAX_THREAD_COUNT = getNumberOfCores() + 1;
    }

    public SyncExecutorService() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_MAX_THREAD_COUNT, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new SyncThreadFactory());
    }

    private static int getNumberOfCores() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            // Use saurabh64's answer
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private static int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }
}
