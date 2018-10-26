package com.salesforce.androidsdk.smartstore;

/**
 * Created by jstafanowski on 18.01.18.
 */

public abstract class CrashReportLogger {

    private static CrashReportLogger INSTANCE = new EmptyLogger();

    public static void setInstance(CrashReportLogger logger) {
        INSTANCE = logger == null ? new EmptyLogger() : logger;
    }

    public static CrashReportLogger getInstance() {
        return INSTANCE;
    }

    public abstract void log(String msg);

    private static class EmptyLogger extends CrashReportLogger {

        @Override
        public void log(String msg) {
            // do nothing
        }
    }
}
