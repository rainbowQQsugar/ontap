package com.salesforce.androidsyncengine.utils;

public class ComparatorUtils {

    public static int compareStrings(String a, String b, boolean nullsLast) {
        if (a == b) return 0;

        if (a == null) {
            return nullsLast ? 1 : -1;
        }
        else if (b == null) {
            return nullsLast ? -1 : 1;
        }
        else {
            return a.compareTo(b);
        }
    }

    public static int compareStrings(String a, String b) {
        return compareStrings(a, b, true);
    }

    public static int compareStringsIgnoreCase(String a, String b, boolean nullsLast) {
        if (a == b) return 0;

        if (a == null) {
            return nullsLast ? 1 : -1;
        }
        else if (b == null) {
            return nullsLast ? -1 : 1;
        }
        else {
            return a.compareToIgnoreCase(b);
        }
    }

    public static int compareStringsIgnoreCase(String a, String b) {
        return compareStringsIgnoreCase(a, b, true);
    }
}
