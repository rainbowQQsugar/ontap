package com.salesforce.androidsyncengine.datamanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jakub Stefanowski on 14.12.2016.
 */
public class DynamicFetchPreferences {

    private static DynamicFetchPreferences instance;

    private static String EMPTY_STRING = "";
    private static char SEMICOLON = ';';
    private static char EQUALS_SIGN = '=';

    private DbHelper dbHelper;

    public static DynamicFetchPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (DynamicFetchPreferences.class) {
                if (instance == null) {
                   instance = new DynamicFetchPreferences(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    private DynamicFetchPreferences(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void storePendingFetch(String objectName, String dynamicFetchName, Map<String, String> params) {
        dbHelper.addPendingFetch(objectName, dynamicFetchName, convertParams(params));
    }

    public void storeFinishedFetch(String objectName, String dynamicFetchName, Map<String, String> params, long time) {
        dbHelper.removePendingFetch(objectName, dynamicFetchName, convertParams(params));
        dbHelper.addLastFetchDate(objectName, dynamicFetchName, convertParams(params), time);
    }

    public void storeFailedFetch(String objectName, String dynamicFetchName, Map<String, String> params) {
        dbHelper.removePendingFetch(objectName, dynamicFetchName, convertParams(params));
    }

    public boolean isFetchPending(String objectName, String dynamicFetchName, Map<String, String> params) {
        return dbHelper.hasPendingFetch(objectName, dynamicFetchName, convertParams(params));
    }

    public long getLastFetchTime(String objectName, String dynamicFetchName, Map<String, String> params) {
        return dbHelper.getLastFetchDate(objectName, dynamicFetchName, convertParams(params));
    }

    public void clear() {
        dbHelper.clear();
    }

    public void deleteOldEntries() {
        dbHelper.deleteOldData();
    }

    private String convertParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) return EMPTY_STRING;

        TreeMap<String, String> sortedParams = new TreeMap<String, String>(params);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (builder.length() != 0) {
                builder.append(SEMICOLON);
            }
            builder.append(entry.getKey()).append(EQUALS_SIGN).append(entry.getValue());
        }

        return builder.toString();
    }

    static class DbHelper extends SQLiteOpenHelper {

        static final int DB_VERSION = 1;

        static final String DB_NAME = "dynamicFetchPrefs.db";

        static final long MAX_DATA_AGE = TimeUnit.DAYS.toMillis(3);

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + LastFetchesTable.TABLE_NAME + " ("
                    + LastFetchesTable.OBJECT_NAME + " TEXT, "
                    + LastFetchesTable.DYNAMIC_FETCH_NAME + " TEXT, "
                    + LastFetchesTable.PARAMS + " TEXT, "
                    + LastFetchesTable.FETCH_DATE + " INTEGER, "
                    + "PRIMARY KEY ("
                        + LastFetchesTable.OBJECT_NAME + ","
                        + LastFetchesTable.DYNAMIC_FETCH_NAME + ","
                        + LastFetchesTable.PARAMS
                    + "))");

            db.execSQL("CREATE TABLE " + PendingFetchesTable.TABLE_NAME + " ("
                    + PendingFetchesTable.OBJECT_NAME + " TEXT, "
                    + PendingFetchesTable.DYNAMIC_FETCH_NAME + " TEXT, "
                    + PendingFetchesTable.PARAMS + " TEXT, "
                    + PendingFetchesTable.DATE_ADDED + " INTEGER, "
                    + "PRIMARY KEY ("
                        + PendingFetchesTable.OBJECT_NAME + ","
                        + PendingFetchesTable.DYNAMIC_FETCH_NAME + ","
                        + PendingFetchesTable.PARAMS
                    + "))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public void addPendingFetch(String objectName, String dynamicFetchName, String params) {
            ContentValues cv = new ContentValues();
            cv.put(PendingFetchesTable.OBJECT_NAME, objectName);
            cv.put(PendingFetchesTable.DYNAMIC_FETCH_NAME, dynamicFetchName);
            cv.put(PendingFetchesTable.PARAMS, params);
            cv.put(PendingFetchesTable.DATE_ADDED, System.currentTimeMillis());

            SQLiteDatabase db = getWritableDatabase();
            db.replace(PendingFetchesTable.TABLE_NAME, null, cv);
        }

        public void removePendingFetch(String objectName, String dynamicFetchName, String params) {
            String whereClause = PendingFetchesTable.OBJECT_NAME + "=? AND "
                    + PendingFetchesTable.DYNAMIC_FETCH_NAME + "=? AND "
                    + PendingFetchesTable.PARAMS + "=?";
            String[] whereArgs = { objectName, dynamicFetchName, params };

            SQLiteDatabase db = getWritableDatabase();
            db.delete(PendingFetchesTable.TABLE_NAME, whereClause, whereArgs);
        }

        public boolean hasPendingFetch(String objectName, String dynamicFetchName, String params) {
            String[] columns = { PendingFetchesTable.OBJECT_NAME };
            String selection = PendingFetchesTable.OBJECT_NAME + "=? AND "
                    + PendingFetchesTable.DYNAMIC_FETCH_NAME + "=? AND "
                    + PendingFetchesTable.PARAMS + "=?";
            String[] selectionArgs = { objectName, dynamicFetchName, params };

            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.query(PendingFetchesTable.TABLE_NAME, columns, selection, selectionArgs, null, null, null, "1");
            boolean hasData = cursor.moveToFirst();
            cursor.close();

            return hasData;
        }

        public void addLastFetchDate(String objectName, String dynamicFetchName, String params, long date) {
            ContentValues cv = new ContentValues();
            cv.put(LastFetchesTable.OBJECT_NAME, objectName);
            cv.put(LastFetchesTable.DYNAMIC_FETCH_NAME, dynamicFetchName);
            cv.put(LastFetchesTable.PARAMS, params);
            cv.put(LastFetchesTable.FETCH_DATE, date);

            SQLiteDatabase db = getWritableDatabase();
            db.replace(LastFetchesTable.TABLE_NAME, null, cv);
        }

        public long getLastFetchDate(String objectName, String dynamicFetchName, String params) {
            String[] columns = { LastFetchesTable.FETCH_DATE };
            String selection = LastFetchesTable.OBJECT_NAME + "=? AND "
                    + LastFetchesTable.DYNAMIC_FETCH_NAME + "=? AND "
                    + LastFetchesTable.PARAMS + "=?";
            String[] selectionArgs = { objectName, dynamicFetchName, params };

            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.query(LastFetchesTable.TABLE_NAME, columns, selection, selectionArgs, null, null, null, "1");
            long lastDate = 0;

            if (cursor.moveToNext()) {
                lastDate = cursor.getLong(0);
            }
            cursor.close();

            return lastDate;
        }

        public void clear() {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(LastFetchesTable.TABLE_NAME, null, null);
            db.delete(PendingFetchesTable.TABLE_NAME, null, null);
        }

        public void deleteOldData() {
            String minDate = Long.toString(System.currentTimeMillis() - MAX_DATA_AGE);
            SQLiteDatabase db = getWritableDatabase();

            String whereClause = LastFetchesTable.FETCH_DATE + "<?";
            String[] whereArgs = { minDate };
            db.delete(LastFetchesTable.TABLE_NAME, whereClause, whereArgs);

            whereClause = PendingFetchesTable.DATE_ADDED + "<?";
            whereArgs = new String[] { minDate };
            db.delete(PendingFetchesTable.TABLE_NAME, whereClause, whereArgs);
        }
    }

    static class LastFetchesTable {
        static final String TABLE_NAME = "LastFetches";

        static final String OBJECT_NAME = "objectName";
        static final String DYNAMIC_FETCH_NAME = "dynamicFetchName";
        static final String PARAMS = "params";
        static final String FETCH_DATE = "fetchDate";
    }

    static class PendingFetchesTable {
        static final String TABLE_NAME = "PendingFetches";

        static final String OBJECT_NAME = "objectName";
        static final String DYNAMIC_FETCH_NAME = "dynamicFetchName";
        static final String PARAMS = "params";
        static final String DATE_ADDED = "dateAdded";
    }
}
