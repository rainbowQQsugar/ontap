package com.salesforce.androidsyncengine.utils;

import static android.text.TextUtils.isEmpty;
import static com.salesforce.androidsyncengine.utils.CharUtils.NEW_LINE;

/**
 * Created by Jakub Stefanowski on 20.10.2017.
 */

public final class StringUtils {

    public static final String EMPTY = "";

    private StringUtils() {
    }

    public static String joinNonEmpty(CharSequence delimiter, CharSequence... parts) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (CharSequence part : parts) {
            if (!isEmpty(part)) {
                if (firstTime) {
                    firstTime = false;
                }
                else {
                    sb.append(delimiter);
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    public static String joinNonEmpty(char delimiter, CharSequence... parts) {
        return joinNonEmpty(String.valueOf(delimiter), parts);
    }


    public static void appendLine(StringBuilder sb, CharSequence part) {
        if (!isEmpty(part)) {
            if (sb.length() > 0) {
                sb.append(NEW_LINE);
            }

            sb.append(part);
        }
    }

    public static void appendLine(StringBuilder sb, char delimiter, CharSequence... parts) {
        appendLine(sb, String.valueOf(delimiter), parts);
    }

    public static void appendLine(StringBuilder sb, CharSequence delimiter, CharSequence... parts) {
        boolean isFirst = true;

        for (CharSequence part : parts) {
            if (!isEmpty(part)) {
                if (isFirst) {
                    isFirst = false;

                    if (sb.length() > 0) {
                        sb.append(NEW_LINE);
                    }
                }
                else {
                    sb.append(delimiter);
                }

                sb.append(part);
            }
        }
    }
}
