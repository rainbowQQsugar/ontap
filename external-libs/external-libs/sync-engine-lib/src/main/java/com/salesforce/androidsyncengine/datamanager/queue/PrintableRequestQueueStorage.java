package com.salesforce.androidsyncengine.datamanager.queue;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Jakub Stefanowski on 16.01.2017.
 */

public class PrintableRequestQueueStorage implements RequestQueueStorage {

    private static final String TAG = "PrintableRequestQueueSt";

    RequestQueueStorage storage;

    public PrintableRequestQueueStorage(RequestQueueStorage storage) {
        this.storage = storage;
    }

    @Override
    public void createStorage() {
        Log.i(TAG, "createStorage()");
        storage.createStorage();
    }

    @Override
    public void deleteStorage() {
        Log.i(TAG, "deleteStorage()");
        storage.deleteStorage();
    }

    @Override
    public JSONObject create(JSONObject data) throws JSONException {
        Log.i(TAG, "create(data=" + data + ")");
        JSONObject resultA = storage.create(data);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONObject upsert(JSONObject data) throws JSONException {
        Log.i(TAG, "upsert(data=" + data + ")");
        JSONObject resultA = storage.upsert(data);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONObject update(JSONObject data, long soupEntryId) throws JSONException {
        Log.i(TAG, "update(data=" + data + ", soupEntryId=" + soupEntryId + ")");
        JSONObject resultA = storage.update(data, soupEntryId);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONArray retrieve(Long... soupEntryIds) throws JSONException {
        Log.i(TAG, "retrieve(soupEntryIds=" + Arrays.toString(soupEntryIds) + ")");
        JSONArray resultA = storage.retrieve(soupEntryIds);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONArray fetchAllRecords() {
        Log.i(TAG, "fetchAllRecords()");
        JSONArray resultA = storage.fetchAllRecords();
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONObject exactQuery(String path, String exactMatchKey) {
        Log.i(TAG, "exactQuery(path=" + path + ", exactMatchKey=" + exactMatchKey + ")");
        JSONObject resultA = storage.exactQuery(path, exactMatchKey);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONObject get(int position) {
        Log.i(TAG, "get(position=" + position +  ")");
        JSONObject resultA = storage.get(position);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public JSONArray queryAll(String path, String exactMatchKey) {
        Log.i(TAG, "queryAll(path=" + path + ", exactMatchKey=" + exactMatchKey + ")");
        JSONArray resultA = storage.queryAll(path, exactMatchKey);
        Log.i(TAG, "result:");
        longLog(toPrettyString(resultA));

        return resultA;
    }

    @Override
    public int deleteAll(String path, String exactMatchKey) throws JSONException {
        Log.i(TAG, "deleteAll(path=" + path + ", exactMatchKey=" + exactMatchKey + ")");
        int resultA = storage.deleteAll(path, exactMatchKey);
        Log.i(TAG, "result:\n" + resultA);

        return resultA;
    }

    @Override
    public void delete(Long... soupEntryIds) {
        Log.i(TAG, "delete(soupEntryIds=" + Arrays.toString(soupEntryIds) + ")");
        storage.delete(soupEntryIds);
    }

    @Override
    public boolean isSingleUserStorage() {
        Log.i(TAG, "isSingleUserStorage()");
        boolean result = storage.isSingleUserStorage();
        Log.i(TAG, "result: " + result);

        return result;
    }

    @Override
    public int getRecordsCount() {
        Log.i(TAG, "getRecordsCount()");
        int result = storage.getRecordsCount();
        Log.i(TAG, "result: " + result);

        return result;
    }

    private String toPrettyString(JSONObject o) {
        try {
            return o == null ? "null" : o.toString(2);
        } catch (JSONException e) {
            Log.w(TAG, e);
            return "null";
        }
    }

    private String toPrettyString(JSONArray o) {
        try {
            return o == null ? "null" : o.toString(2);
        } catch (JSONException e) {
            Log.w(TAG, e);
            return "null";
        }
    }

    private static final char NEW_LINE = '\n';

    private void longLog(String string) {
        if (TextUtils.isEmpty(string)) {
            Log.i(TAG, string);
            return;
        }

        int start = 0;

        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == NEW_LINE) {
                Log.i(TAG, string.substring(start, i + 1));
                start = i + 1;
            }
        }

        if (start < string.length()) {
            Log.i(TAG, string.substring(start, string.length()));
        }
    }
}
