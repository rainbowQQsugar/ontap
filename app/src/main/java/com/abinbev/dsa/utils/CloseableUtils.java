package com.abinbev.dsa.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by jstafanowski on 08.02.18.
 */

public final class CloseableUtils {

    private static final String TAG = CloseableUtils.class.getSimpleName();

    private CloseableUtils() { }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }
}
