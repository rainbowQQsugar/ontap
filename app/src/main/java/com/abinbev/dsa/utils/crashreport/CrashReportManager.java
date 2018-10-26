package com.abinbev.dsa.utils.crashreport;

import android.content.Context;

import java.util.Map;

/**
 * Created by jstafanowski on 29.01.18.
 */

public interface CrashReportManager {

    void init(Context context);

    void setUserDetails(String userName, String userId);

    void log(String message);

    void logException(Exception exception);

    void logException(Exception exception, Map<String, String> params);
}
