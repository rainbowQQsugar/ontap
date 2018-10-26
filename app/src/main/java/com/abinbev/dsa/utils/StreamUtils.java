package com.abinbev.dsa.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jstafanowski on 08.02.18.
 */

public final class StreamUtils {

    private static final int BUF_SIZE = 8192;

    private StreamUtils() {}

    public static long copy(InputStream from, OutputStream to) throws IOException {
        if (from == null || to == null) throw new NullPointerException();

        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }
}
