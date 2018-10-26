package com.salesforce.androidsyncengine.datamanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jakub Stefanowski on 06.03.2017.
 */

public class SQLiteTempIdContainer extends AbstractTempIdContainer {

    private static final String DB_NAME = "temp_id_container.db";

    private static final int DB_VERSION = 1;

    private final TempIdSqliteHelper sqliteHelper;

    private SQLiteDatabase database;

    public SQLiteTempIdContainer(Context context) {
        this.sqliteHelper = new TempIdSqliteHelper(context);
    }

    @Override
    public void insertSalesforceId(String localId, String salesforceId) {
        sqliteHelper.insert(getDatabase(), localId, salesforceId, System.currentTimeMillis());
    }

    @Override
    public String getSalesforceId(String tempId) {
        return sqliteHelper.getSalesforceId(getDatabase(), tempId);
    }

    @Override
    public int deleteOlderThan(long date) {
        return sqliteHelper.deleteOlderThan(getDatabase(), date);
    }

    @Override
    public int deleteAll() {
        return sqliteHelper.deleteAll(getDatabase());
    }

    @Override
    protected void iterate(Visitor visitor) {
        String[] columns = { TempIdSqliteHelper.COLUMN_LOCAL_ID,
                TempIdSqliteHelper.COLUMN_SALESFORCE_ID };

        Cursor cursor = sqliteHelper.getAll(getDatabase(), columns);
        while (cursor.moveToNext()) {
            visitor.visit(cursor.getString(0), cursor.getString(1));
        }

        cursor.close();
    }

    private SQLiteDatabase getDatabase() {
        if (database == null) {
            database = sqliteHelper.getWritableDatabase();
        }

        return database;
    }

    private static class TempIdSqliteHelper extends SQLiteOpenHelper {

        static final String TABLE_TEMP_IDS = "TemporaryIds";

        static final String COLUMN_LOCAL_ID = "LocalId";

        static final String COLUMN_SALESFORCE_ID = "SalesforceId";

        static final String COLUMN_CREATED_DATE = "CreatedDate";

        public TempIdSqliteHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String createTableStmt = "CREATE TABLE IF NOT EXISTS " + TABLE_TEMP_IDS + " (" +
                    COLUMN_LOCAL_ID + " TEXT PRIMARY KEY" + ", " +
                    COLUMN_SALESFORCE_ID + " TEXT" + ", " +
                    COLUMN_CREATED_DATE + " INTEGER" +
                    ")";

            db.execSQL(createTableStmt);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public long insert(SQLiteDatabase db, String localId, String salesforceId, long createdDate) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_LOCAL_ID, localId);
            cv.put(COLUMN_SALESFORCE_ID, salesforceId);
            cv.put(COLUMN_CREATED_DATE, createdDate);

            return db.insertWithOnConflict(TABLE_TEMP_IDS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }

        public String getSalesforceId(SQLiteDatabase db, String localId) {
            String[] columns = { COLUMN_SALESFORCE_ID };
            String selection = COLUMN_LOCAL_ID + "=?";
            String[] selectionArgs = { localId };
            Cursor cursor = db.query(TABLE_TEMP_IDS, columns, selection, selectionArgs, null, null, null);

            String result = null;

            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }

            cursor.close();

            return result;
        }

        public Cursor getAll(SQLiteDatabase db, String[] columns) {
            return db.query(TABLE_TEMP_IDS, columns, null, null, null, null, null);
        }

        public int deleteOlderThan(SQLiteDatabase db, long date) {
            String whereClause = COLUMN_CREATED_DATE + "<?";
            String[] whereArgs = { Long.toString(date) };
            return db.delete(TABLE_TEMP_IDS, whereClause, whereArgs);
        }

        public int deleteAll(SQLiteDatabase db) {
            return db.delete(TABLE_TEMP_IDS, null, null);
        }
    }
}
