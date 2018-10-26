package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Jakub Stefanowski on 25.05.2017.
 */

public class MobileSyncReporter {

    private static final String TAG = MobileSyncReporter.class.getSimpleName();

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    private static final String OBJECT_NAME = "ONTAP__Mobile_Sync_Log__c";
    private static final String FIELD_APP_VERSION = "ONTAP__App_Version__c";
    private static final String FIELD_CONNECTION_TYPE = "ONTAP__Connection_Type__c";
    private static final String FIELD_DEVICE_TYPE = "ONTAP__Device_Type__c";
    private static final String FIELD_DEVICE_VERSION = "ONTAP__Device_Version__c";
    private static final String FIELD_OS_VERSION = "ONTAP__OS_Version__c";
    private static final String FIELD_SYNC_END = "ONTAP__Sync_End_Time__c";
    private static final String FIELD_SYNC_START = "ONTAP__Sync_Start_Time__c";
    private static final String FIELD_SYNC_STATUS = "ONTAP__Sync_Status__c";
    private static final String FIELD_USER = "ONTAP__User__c";

    public enum SyncType {
        FULL, DELTA
    }

    enum DeviceType {
        MOBILE, TABLET_7_INCH, TABLET_10_INCH
    }

    private final SyncHelper syncHelper;

    private final Context context;

    private final Handler handler = new Handler(createNewLooper());

    private String syncReportId;

    private boolean isStarted;

    private static Looper createNewLooper() {
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        return ht.getLooper();
    }

    public MobileSyncReporter(SyncHelper syncHelper, Context context) {
        this.syncHelper = syncHelper;
        this.context = context;
    }

    public void reportSyncStart(long startTime, String userId, SyncType type) {
        if (isStarted) return;

        try {
            Log.v(TAG, "reportSyncStart");
            syncReportId = null;
            isStarted = true;
            handler.post(new ReportStartTask(this, type, userId, startTime));
        }
        catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    public void reportSyncEnd(long endTime) {
        if (!isStarted) return;

        String lastSyncReportId = syncReportId;
        if (lastSyncReportId == null) return;

        try {
            Log.v(TAG, "reportSyncEnd");
            isStarted = false;
            syncReportId = null;
            handler.post(new ReportEndTask(this, lastSyncReportId, endTime));
        }
        catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    private String getAppVersion() {
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }

        return "UNKNOWN";
    }

    private String getDeviceType() {
        Resources resources = context.getResources();
        if (resources.getBoolean(R.bool.is10InchTablet)) {
            return DeviceType.TABLET_10_INCH.toString();
        }
        else if (resources.getBoolean(R.bool.is7InchTablet)) {
            return DeviceType.TABLET_7_INCH.toString();
        }
        else {
            return DeviceType.MOBILE.toString();
        }
    }

    private String getNetworkType() {
        return DeviceNetworkUtils.getNetworkType(context);
    }

    void setSyncReportId(String syncReportId) {
        this.syncReportId = syncReportId;
    }

    Context getContext() {
        return context;
    }

    SyncHelper getSyncHelper() {
        return syncHelper;
    }

    void setStarted(boolean started) {
        isStarted = started;
    }

    private static class ReportStartTask implements Runnable {

        private final MobileSyncReporter reporter;

        private final String userId;

        private final long startTime;

        private final SyncType syncType;

        private ReportStartTask(MobileSyncReporter reporter, SyncType syncType, String userId, long startTime) {
            this.reporter = reporter;
            this.userId = userId;
            this.startTime = startTime;
            this.syncType = syncType;
        }

        @Override
        public void run() {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put(FIELD_CONNECTION_TYPE, reporter.getNetworkType());
                data.put(FIELD_DEVICE_TYPE, reporter.getDeviceType());
                data.put(FIELD_DEVICE_VERSION, Build.MODEL);
                data.put(FIELD_OS_VERSION, Build.VERSION.RELEASE);
                data.put(FIELD_APP_VERSION, reporter.getAppVersion());
                data.put(FIELD_SYNC_STATUS, syncType.toString());
                data.put(FIELD_USER, userId);
                data.put(FIELD_SYNC_START, DATE_FORMAT.format(new Date(startTime)));

                SyncHelper syncHelper = reporter.getSyncHelper();
                RestResponse response = syncHelper.sendCreateRequest(data, OBJECT_NAME);
                if (response.isSuccess()) {
                    JSONObject jsonResponse = response.asJSONObject();
                    reporter.setSyncReportId(jsonResponse.getString("id"));
                }
            }
            catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    private static class ReportEndTask implements Runnable {

        private final MobileSyncReporter reporter;

        private final String syncReportId;

        private final long endTime;

        private ReportEndTask(MobileSyncReporter reporter, String syncReportId, long endTime) {
            this.reporter = reporter;
            this.syncReportId = syncReportId;
            this.endTime = endTime;
        }

        @Override
        public void run() {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put(FIELD_SYNC_END, DATE_FORMAT.format(new Date(endTime)));

                SyncHelper syncHelper = reporter.getSyncHelper();
                syncHelper.sendUpdateRequest(syncReportId, data, OBJECT_NAME);
            }
            catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }
}
