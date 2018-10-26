package com.salesforce.androidsyncengine.datamanager.queue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Stefanowski on 16.01.2017.
 */

public class SqliteRequestQueueStorage implements RequestQueueStorage {

    private static final String TAG = "SqliteRequestQueueSt...";

    private static final int DB_VERSION = 1;

    // JSON fields added to soup element on insert/update
    public static final String SOUP_ENTRY_ID = "_soupEntryId";
    public static final String SOUP_LAST_MODIFIED_DATE = "_soupLastModifiedDate";

    private SqliteHelper sqliteHelper;

    private SQLiteDatabase database;

    private String userId;

    private String orgId;

    public SqliteRequestQueueStorage(Context context, String dbName, String userId, String orgId) {
        this.sqliteHelper = new SqliteHelper(context, dbName, DB_VERSION);
        this.userId = userId;
        this.orgId = orgId;
    }

    @Override
    public void createStorage() {
        SQLiteDatabase db = getDatabase();
        sqliteHelper.createTablesIfNotExists(db);
    }

    @Override
    public void deleteStorage() {
        SQLiteDatabase db = getDatabase();
        sqliteHelper.dropTables(db);
    }

    @Override
    public JSONObject upsert(JSONObject data) throws JSONException {
        long entryId = -1;
        if (data.has(SOUP_ENTRY_ID)) {
            entryId = data.getLong(SOUP_ENTRY_ID);
        }

        if (entryId == -1) {
            return create(data);
        }
        else {
            return update(data, entryId);
        }
    }

    @Override
    public JSONObject create(JSONObject data) throws JSONException {
        SQLiteDatabase db = getDatabase();
        long now = System.currentTimeMillis();
        data.put(SOUP_LAST_MODIFIED_DATE, now);

        ContentValues cv = new ContentValues();
        cv.put(SqliteHelper.COLUMN_USER_ID, userId);
        cv.put(SqliteHelper.COLUMN_ORG_ID, orgId);
        cv.put(SqliteHelper.COLUMN_SOUP, data.toString());
        cv.put(SqliteHelper.COLUMN_CREATED, now);
        cv.put(SqliteHelper.COLUMN_LAST_MODIFIED, now);
        appendIndexedPaths(SqliteHelper.QUEUE_INDEX_SPEC, data, cv);

        long newId = db.insert(SqliteHelper.TABLE_QUEUE, null, cv);
        data.put(SOUP_ENTRY_ID, newId);

        return data;
    }

    @Override
    public JSONObject update(JSONObject data, long soupEntryId) throws JSONException {
        SQLiteDatabase db = getDatabase();
        long now = System.currentTimeMillis();

        data.put(SOUP_ENTRY_ID, soupEntryId);
        data.put(SOUP_LAST_MODIFIED_DATE, now);

        ContentValues cv = new ContentValues();
        cv.put(SqliteHelper.COLUMN_USER_ID, userId);
        cv.put(SqliteHelper.COLUMN_ORG_ID, orgId);
        cv.put(SqliteHelper.COLUMN_SOUP, data.toString());
        cv.put(SqliteHelper.COLUMN_LAST_MODIFIED, now);
        appendIndexedPaths(SqliteHelper.QUEUE_INDEX_SPEC, data, cv);

        String whereClause = String.format("%s=%d AND %s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_ID, soupEntryId,
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId);

        int updated = db.update(SqliteHelper.TABLE_QUEUE, cv, whereClause, null);

        Log.v(TAG, "Updated records: " + updated);

        return data;
    }

    @Override
    public JSONArray retrieve(Long... soupEntryIds) throws JSONException {
        SQLiteDatabase db = getDatabase();
        JSONArray result = new JSONArray();

        for (Long id : soupEntryIds) {
            String whereClause = String.format("%s=%d AND %s='%s' AND %s='%s'",
                    SqliteHelper.COLUMN_ID, id,
                    SqliteHelper.COLUMN_USER_ID, userId,
                    SqliteHelper.COLUMN_ORG_ID, orgId);
            Cursor cursor = db.query(SqliteHelper.TABLE_QUEUE, null, whereClause, null, null, null, null, "1");
            JSONObject jsonObject = readSingleRecord(cursor);
            if (jsonObject != null) {
                result.put(jsonObject);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public JSONArray fetchAllRecords() {
        SQLiteDatabase db = getDatabase();

        String whereClause = String.format("%s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId);

        Cursor cursor = db.query(SqliteHelper.TABLE_QUEUE, null, whereClause, null, null, null, null);
        JSONArray result = readAllRecords(cursor);
        cursor.close();

        return result;
    }

    @Override
    public JSONObject get(int position) {

        SQLiteDatabase db = getDatabase();

        String whereClause = String.format("%s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId);
        String limit = position + ", 1"; // offset, limit

        Cursor cursor = db.query(SqliteHelper.TABLE_QUEUE, null, whereClause, null, null, null, null, limit);
        JSONObject result = readSingleRecord(cursor);
        cursor.close();

        return result;
    }

    @Override
    public JSONObject exactQuery(String path, String exactMatchKey) {
        SQLiteDatabase db = getDatabase();
        IndexSpec indexSpec = sqliteHelper.getIndexSpecByPath(path);
        if (indexSpec == null) {
            Log.w(TAG, "There is no IndexSpec for path: " + path);
            return null;
        }

        String whereClause = String.format("%s='%s' AND %s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId,
                indexSpec.columnName, exactMatchKey);
        Cursor cursor = db.query(SqliteHelper.TABLE_QUEUE, null, whereClause, null, null, null, null, "1");
        JSONObject result = readSingleRecord(cursor);
        cursor.close();

        return result;
    }

    @Override
    public JSONArray queryAll(String path, String exactMatchKey) {
        SQLiteDatabase db = getDatabase();
        IndexSpec indexSpec = sqliteHelper.getIndexSpecByPath(path);
        if (indexSpec == null) {
            Log.w(TAG, "There is no IndexSpec for path: " + path);
            return null;
        }

        String whereClause = String.format("%s='%s' AND %s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId,
                indexSpec.columnName, exactMatchKey);

        Cursor cursor = db.query(SqliteHelper.TABLE_QUEUE, null, whereClause, null, null, null, null);
        JSONArray result = readAllRecords(cursor);
        cursor.close();

        return result;
    }

    @Override
    public int deleteAll(String path, String exactMatchKey) throws JSONException {
        SQLiteDatabase db = getDatabase();
        IndexSpec indexSpec = sqliteHelper.getIndexSpecByPath(path);
        if (indexSpec == null) {
            Log.w(TAG, "There is no IndexSpec for path: " + path);
            return 0;
        }

        String whereClause = String.format("%s='%s' AND %s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId,
                indexSpec.columnName, exactMatchKey);

        return db.delete(SqliteHelper.TABLE_QUEUE, whereClause, null);
    }

    @Override
    public void delete(Long... soupEntryIds) {
        SQLiteDatabase db = getDatabase();

        db.beginTransaction();
        try {
            for (Long id : soupEntryIds) {
                String whereClause = String.format("%s=%d AND %s='%s' AND %s='%s'",
                        SqliteHelper.COLUMN_ID, id,
                        SqliteHelper.COLUMN_USER_ID, userId,
                        SqliteHelper.COLUMN_ORG_ID, orgId);
                db.delete(SqliteHelper.TABLE_QUEUE, whereClause, null);
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public int getRecordsCount() {
        SQLiteDatabase db = getDatabase();

        String whereClause = String.format("%s='%s' AND %s='%s'",
                SqliteHelper.COLUMN_USER_ID, userId,
                SqliteHelper.COLUMN_ORG_ID, orgId);

        Cursor cursor = db.rawQuery(String.format("SELECT count(*) FROM %s WHERE %s", SqliteHelper.TABLE_QUEUE, whereClause), null);
        int result = cursor.moveToNext() ? cursor.getInt(0) : 0;
        cursor.close();

        return result;
    }

    @Override
    public boolean isSingleUserStorage() {
        return false;
    }

    private SQLiteDatabase getDatabase() {
        if (database == null) {
            database = sqliteHelper.getWritableDatabase();
        }

        return database;
    }

    private void appendIndexedPaths(IndexSpec[] indexedPaths, JSONObject json, ContentValues cv) {
        if (indexedPaths == null || json == null || cv == null) return;

        for (IndexSpec spec : indexedPaths) {
            Object value = json.opt(spec.path);

            if (value == null) {
                cv.putNull(spec.columnName);
            }
            else {
                switch (spec.type) {
                    case floating:
                        cv.put(spec.columnName, ((Number) value).doubleValue());
                        break;

                    case string:
                    case full_text:
                        cv.put(spec.columnName, value.toString());
                        break;

                    case integer:
                        cv.put(spec.columnName, ((Number) value).longValue());
                        break;
                }
            }
        }
    }

    private JSONObject readSingleRecord(Cursor cursor) {
        if (cursor.moveToNext()) {
            try {
                return parseRecord(cursor);
            } catch (JSONException e) {
                Log.w(TAG, e);
            }
        }

        return null;
    }

    private JSONArray readAllRecords(Cursor cursor) {
        JSONArray result = new JSONArray();
        while (cursor.moveToNext()) {
            try {
                result.put(parseRecord(cursor));
            } catch (JSONException e) {
                Log.w(TAG, e);
            }
        }

        return result;
    }

    private JSONObject parseRecord(Cursor cursor) throws JSONException {
        int soupColumnIndex = cursor.getColumnIndex(SqliteHelper.COLUMN_SOUP);
        int idColumnIndex = cursor.getColumnIndex(SqliteHelper.COLUMN_ID);
        String soup = cursor.getString(soupColumnIndex);
        long id = cursor.getLong(idColumnIndex);

        JSONObject result = new JSONObject(soup);
        result.put(SOUP_ENTRY_ID, id);

        return result;
    }

    private String toString(Cursor c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.getColumnCount(); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(c.getColumnName(i)).append(": ").append(c.getString(i));
        }

        return sb.toString();
    }

    private static class SqliteHelper extends SQLiteOpenHelper {

        static String TABLE_QUEUE = "Queue";
        static String COLUMN_ID = "Id";
        static String COLUMN_SOUP = "Soup";
        static String COLUMN_CREATED = "Created";
        static String COLUMN_LAST_MODIFIED = "Modified";
        static String COLUMN_USER_ID = "UserId";
        static String COLUMN_ORG_ID = "OrgId";

        static IndexSpec[] QUEUE_INDEX_SPEC = {
            new IndexSpec("Id", SmartStore.Type.string, "RequestId"),
            new IndexSpec("objectType", SmartStore.Type.string, "RequestObjectType"),
            new IndexSpec("operation", SmartStore.Type.string, "RequestOperation")
        };

        public SqliteHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTablesIfNotExists(db);
        }

        public void createTablesIfNotExists(SQLiteDatabase db) {
            List<String> createIndexStmts = new ArrayList<String>();
            StringBuilder createTableStmt = new StringBuilder();

            createTableStmt.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_QUEUE).append(" (")
                    .append(COLUMN_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT").append(", ")
                    .append(COLUMN_SOUP).append(" TEXT").append(", ")
                    .append(COLUMN_USER_ID).append(" TEXT").append(", ")
                    .append(COLUMN_ORG_ID).append(" TEXT").append(", ")
                    .append(COLUMN_CREATED).append(" INTEGER").append(", ")
                    .append(COLUMN_LAST_MODIFIED).append(" INTEGER");

            for (IndexSpec indexSpec : QUEUE_INDEX_SPEC) {
                String columnName = indexSpec.columnName;
                String columnType = indexSpec.type.getColumnType();
                createTableStmt.append(", ").append(columnName).append(" ").append(columnType);

                // for fts
                if (indexSpec.type == SmartStore.Type.full_text) {
                    throw new IllegalStateException("FTS is not supported in Sqlite Request Queue.");
                }

                // for create index
                String indexName = columnName + "_idx";
                createIndexStmts.add(String.format("CREATE INDEX IF NOT EXISTS %s on %s ( %s )", indexName, TABLE_QUEUE, columnName));
            }
            createTableStmt.append(")");

            db.execSQL(createTableStmt.toString());
            for (String createIndexStmt : createIndexStmts) {
                db.execSQL(createIndexStmt);
            }
        }

        public void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + SqliteHelper.TABLE_QUEUE);

            for (IndexSpec indexSpec : QUEUE_INDEX_SPEC) {
                String columnName = indexSpec.columnName;
                String indexName = columnName + "_idx";
                db.execSQL(String.format("DROP INDEX IF EXISTS %s", indexName));
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public IndexSpec getIndexSpecByPath(String path) {
            for (IndexSpec spec : QUEUE_INDEX_SPEC) {
                if (spec.path.equals(path)) return spec;
            }

            return null;
        }
    }
}
