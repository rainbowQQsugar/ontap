package com.abinbev.dsa.utils.crashreport;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.abinbev.dsa.BuildConfig;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.AbstractCrashesListener;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.model.ErrorReport;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jstafanowski on 29.01.18.
 */

public class AppCenterCrashReportManager implements CrashReportManager {

    private static final String TAG = "AppCenterCrashReportMgr";

    private static final String APP_CENTER_KEY = "e754907c-f618-4ffb-abe2-7e3b1180a4c6";

    private static final String PREF_NAME = "app_center_crash_info";

    private static final String PREF_KEY_USER_NAME = "userName";
    private static final String PREF_KEY_USER_ID = "userId";
    private static final String PREF_KEY_LAST_LOG = "lastLog";

    private static final String CRASH_DATA_FILE_NAME = "data.txt";

    private SharedPreferences preferences;

    private AbstractCrashesListener customListener = new AbstractCrashesListener() {
        @Override
        public Iterable<ErrorAttachmentLog> getErrorAttachments(ErrorReport report) {
            ErrorAttachmentLog textLog = ErrorAttachmentLog.attachmentWithText(
                    getAdditionalErrorData(), CRASH_DATA_FILE_NAME);
            return Collections.singletonList(textLog);
        }
    };

    @Override
    public void init(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Application application = (Application) context.getApplicationContext();
        AppCenter.start(application, APP_CENTER_KEY, Analytics.class, Crashes.class);
        Crashes.setListener(customListener);

        if (BuildConfig.DEBUG) {
            AppCenter.setEnabled(false);
        }
    }

    @Override
    public void setUserDetails(String userName, String userId) {
        preferences.edit()
                .putString(PREF_KEY_USER_NAME, userName)
                .putString(PREF_KEY_USER_ID, userId)
                .apply();
    }

    @Override
    public void log(String message) {
        preferences.edit()
                .putString(PREF_KEY_LAST_LOG, message)
                .apply();
    }

    @Override
    public void logException(Exception exception) {
        logException(exception, new HashMap<>());
    }

    @Override
    public void logException(Exception exception, Map<String, String> params) {
        if (exception == null) throw new NullPointerException();

        HashMap<String, String> paramsCopy = new HashMap<>(params);
        paramsCopy.put("exceptionType", exception.getClass().getSimpleName());
        paramsCopy.put("exceptionStackTrace", getStackTrace(exception));

        Analytics.trackEvent("Exception", paramsCopy);
    }

    private String getAdditionalErrorData() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", preferences.getString(PREF_KEY_USER_NAME, null));
            jsonObject.put("userId", preferences.getString(PREF_KEY_USER_ID, null));
            jsonObject.put("lastLog", preferences.getString(PREF_KEY_LAST_LOG, null));
            return jsonObject.toString(2);
        }
        catch (Exception e) {
            Log.w(TAG, e);
            return e.getMessage();
        }
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
