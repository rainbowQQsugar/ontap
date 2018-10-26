package com.salesforce.androidsyncengine.syncsteps.fetchdeleted;

import android.util.Log;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.synchelper.FetchResponseHandler;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subresponse;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that handles response after fetching deleted records. It is responsible for deleting them
 * locally.
 */
class DeleteRecordsResponseHandler extends FetchResponseHandler {

    private static final String TAG = "DeleteRecordsResponseHa";

    private static final String SMALL_ID_FIELD = "id";

    private static final SimpleDateFormat soqlQueryDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US); // Mon, 14 Apr 2014 21:40:15 GMT
    private static final SimpleDateFormat deleteDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.US);
    static {
        soqlQueryDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        deleteDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final String objectName;
    private final boolean containsContentFiles;
    private final DownloadHelper downloadHelper;
    private final SmartStoreDataManagerImpl dataManager;
    private String serverDate;

    protected DeleteRecordsResponseHandler(String objectName, boolean containsContentFiles, SyncControls syncControls) {
        super(syncControls.getContext(), syncControls.getSyncHelper());
        this.objectName = objectName;
        this.containsContentFiles = containsContentFiles;
        this.downloadHelper = syncControls.getDownloadHelper();
        this.dataManager = syncControls.getDataManager();
    }

    @Override
    public Subrequest handleResponse(RestRequest originalRequest, RestResponse originalResponse, Subrequest subrequest, Subresponse subresponse) throws SyncException {
        if (serverDate == null) {
            serverDate = getServerDateString(originalResponse);
        }
        return super.handleResponse(originalRequest, originalResponse, subrequest, subresponse);
    }

    @Override
    protected void handleRecords(JSONArray records, int startIndex, boolean isLast) throws SyncException {
        deleteLocalRecords(records);
        Log.i(TAG, "Deleted record count: " + records.length() + " for: " + objectName);

        // if we got here then everything was good at least for the first set of getDeleted records.
        PreferenceUtils.putLastDeletedTime(objectName, serverDate, getContext());
    }

    @Override
    protected JSONArray getRecords(JSONObject jsonResponse) throws JSONException {
        return jsonResponse.getJSONArray("deletedRecords");
    }

    @Override
    protected int getTotalSize(JSONObject jsonResponse) throws JSONException {
        return -1; // This request is not returning total size.
    }

    private void deleteLocalRecords(JSONArray records) {

        Log.i(TAG, "in deleteLocalRecords for: " + objectName + " with length: " + records.length());

        for (int i = 0; i < records.length(); i++) {
            JSONObject jsonObject = records.optJSONObject(i);
            if (jsonObject != null) {
                String id = jsonObject.optString(SMALL_ID_FIELD);
                if (id != null) {
                    if (containsContentFiles) {
                        downloadHelper.removeContentFiles(objectName, id);
                    }

                    dataManager.deleteRecordWithoutAddingToQueue(objectName, id);
                }
            }
        }
    }

    private static String getServerDateString(RestResponse response) {
        return getServerDateString(response.getAllHeaders());
    }

    private static String getServerDateString(Map<String, List<String>> headers) {
        String dateString = null;
        try {
            Log.i(TAG, "headers: " + headers.toString());
            String dateValue = headers.get("Date").get(0);
            if (dateValue == null) {
                Date serverDate = new Date();
                dateString = soqlQueryDateFormat.format(serverDate);
                return dateString;
            }
            Date serverDate = serverDateFormat.parse(dateValue);
            dateString = soqlQueryDateFormat.format(serverDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateString;
    }
}