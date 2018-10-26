package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jakub Stefanowski on 11.10.2016.
 */
class SyncEnginePrefs {

    private static final String KEY_FAIL_DATE = "fail_date";
    private static final String KEY_FAIL_SOUP_IDS = "fail_soup_ids";

    private SharedPreferences mPrefs;

    private Gson mGson;

    public SyncEnginePrefs(Context context) {
        mPrefs = context.getSharedPreferences("sync_engine_preferences", Context.MODE_PRIVATE);
        mGson = new Gson();
    }

    public void setFailData(long date, List<Long> soupIds) {
        String idsString = soupIds == null ? null : mGson.toJson(soupIds);
        mPrefs.edit()
                .putLong(KEY_FAIL_DATE, date)
                .putString(KEY_FAIL_SOUP_IDS, idsString)
                .commit();
    }

    public void updateIds(List<Long> soupIds) {
        String idsString = soupIds == null ? null : mGson.toJson(soupIds);
        mPrefs.edit()
                .putString(KEY_FAIL_SOUP_IDS, idsString)
                .commit();
    }

    public long getFailDate() {
        return mPrefs.getLong(KEY_FAIL_DATE, -1L);
    }

    public List<Long> getFailedSoupIds() {
        String idsJson = mPrefs.getString(KEY_FAIL_SOUP_IDS, null);

        Type listType = new TypeToken<List<Long>>() {}.getType();
        List<Long> result = idsJson == null ? null : (List<Long>) mGson.fromJson(idsJson, listType);

        return result == null ? Collections.<Long>emptyList() : result;
    }

    public void clear() {
        mPrefs.edit().clear().commit();
    }
}
