package com.salesforce.androidsyncengine.utils;

/**
 * Created by Jakub Stefanowski on 23.02.2017.
 */

public final class TransactionUtils {

    private TransactionUtils() { }

    public static long generateTransactionId() {
        return (long) (Math.random() * 1000000000L);
    }
}
