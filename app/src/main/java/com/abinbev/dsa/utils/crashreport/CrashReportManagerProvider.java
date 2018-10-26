package com.abinbev.dsa.utils.crashreport;

/**
 * Created by jstafanowski on 29.01.18.
 */

public class CrashReportManagerProvider {

    private static CrashReportManager instance;

    public static CrashReportManager getInstance() {
        if (instance == null) {
            synchronized (CrashReportManagerProvider.class) {
                if (instance == null) {
                    instance = new AppCenterCrashReportManager();
                }
            }
        }

        return instance;
    }
}
