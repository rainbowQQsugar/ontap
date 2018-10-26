package com.abinbev.dsa.utils.crashreport;

import android.content.Context;

import com.abinbev.dsa.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.Collections;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by jstafanowski on 29.01.18.
 */

public class CrashlyticsCrashReportManager implements CrashReportManager {

    @Override
    public void init(Context context) {
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(context, new Crashlytics.Builder()
                .core(core)
                .build());
    }

    @Override
    public void setUserDetails(String userName, String userId) {
        Crashlytics.setUserIdentifier(userId);
        Crashlytics.setUserName(userName);
    }

    @Override
    public void log(String message) {
        Crashlytics.log(message);
    }

    @Override
    public void logException(Exception exception) {
        logException(exception, Collections.emptyMap());
    }

    @Override
    public void logException(Exception exception, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Crashlytics.setString(entry.getKey(), entry.getValue());
        }

        Crashlytics.logException(exception);
    }
}
