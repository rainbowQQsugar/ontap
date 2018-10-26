package com.abinbev.dsa.syncmanifest;

import java.util.Collection;

/**
 * Created by Jakub Stefanowski on 06.04.2017.
 */

class StringConverter {

    private static final char COMMA = ',';
    private static final char QUOTE = '\'';
    private static final String EMPTY = "";

    public String convert(Object o) {
        if (o == null) return EMPTY;

        if (o instanceof Collection) {
            return collectionToString((Collection<?>) o);
        }
        else if (o instanceof Object[]) {
            return arrayToString((Object[]) o);
        }
        else {
            return String.valueOf(o);
        }
    }

    private String arrayToString(Object[] arr) {
        StringBuilder sb = new StringBuilder();

        for (Object item : arr) {
            appendItem(item, sb);
        }

        return sb.toString();
    }

    private String collectionToString(Collection<?> collection) {
        StringBuilder sb = new StringBuilder();

        for (Object item : collection) {
            appendItem(item, sb);
        }

        return sb.toString();
    }

    private void appendItem(Object item, StringBuilder sb) {
        if (sb.length() != 0) {
            sb.append(COMMA);
        }

        if (item instanceof String) {
            sb.append(QUOTE).append(item).append(QUOTE);
        }
        // If not string save without quotes.
        else {
            sb.append(String.valueOf(item));
        }
    }
}
