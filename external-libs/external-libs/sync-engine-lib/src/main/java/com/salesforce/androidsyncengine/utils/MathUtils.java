package com.salesforce.androidsyncengine.utils;

/**
 * Created by Jakub Stefanowski on 10.05.2017.
 */

public final class MathUtils {

    private MathUtils() {}

    public static int ceilInt(double num) {
        return (int) Math.round(Math.ceil(num));
    }
}
