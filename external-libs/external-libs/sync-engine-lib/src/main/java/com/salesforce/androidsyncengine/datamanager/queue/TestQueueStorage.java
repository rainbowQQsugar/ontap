package com.salesforce.androidsyncengine.datamanager.queue;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class to test if two RequestQueueStorages behaves the same way.
 *
 * Created by Jakub Stefanowski on 16.01.2017.
 */

public class TestQueueStorage {

    private static final String TAG = "TestQueueStorage";

    RequestQueueStorage storageA;
    RequestQueueStorage storageB;

    public TestQueueStorage(RequestQueueStorage storageA, RequestQueueStorage storageB) {
        this.storageA = storageA;
        this.storageB = storageB;
    }

    public void runTests() throws JSONException {

        Log.i(TAG, "===== create first run ======");
        testCreate();

        Log.i(TAG, "===== create second run ======");
        testCreate();

        Log.i(TAG, "===== update first run ======");
        testUpdate();

        Log.i(TAG, "===== count first run ======");
        testRecordsCount();

        throw new RuntimeException("Testing finished!");
    }

    private void testCreate() throws JSONException {
        setup();

        // === create()

        JSONObject returnedJsonA = storageA.create(createTestNoteObject());
        JSONObject returnedJsonB = storageB.create(createTestNoteObject());

        if (!equals(returnedJsonA, returnedJsonB)) {
            Log.e(TAG, "testCreate() - returned objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(returnedJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(returnedJsonB));
        }

        returnedJsonA = storageA.create(createTestNoteObject2());
        returnedJsonB = storageB.create(createTestNoteObject2());

        if (!equals(returnedJsonA, returnedJsonB)) {
            Log.e(TAG, "testCreate() - returned objects 2 are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(returnedJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(returnedJsonB));
        }

        // === retrieve()

        JSONArray retrievedJsonA = storageA.retrieve(returnedJsonA.getLong("_soupEntryId"));
        JSONArray retrievedJsonB = storageB.retrieve(returnedJsonA.getLong("_soupEntryId"));

        if (!equals(retrievedJsonA, retrievedJsonB)) {
            Log.e(TAG, "testCreate() - retrieved objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(retrievedJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(retrievedJsonB));
        }

        retrievedJsonA = storageA.retrieve(-1L);
        retrievedJsonB = storageB.retrieve(-1L);

        if (!equals(retrievedJsonA, retrievedJsonB)) {
            Log.e(TAG, "testCreate() - retrieved 2 objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(retrievedJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(retrievedJsonB));
        }

        // === fetchAllRecords()

        JSONArray fetchAllJsonA = storageA.fetchAllRecords();
        JSONArray fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testCreate() - fetch all objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        // === exactQuery()

        JSONObject exactQueryJsonA = storageA.exactQuery("Id", "002q0000001ZjVMAA0");
        JSONObject exactQueryJsonB = storageB.exactQuery("Id", "002q0000001ZjVMAA0");

        if (!equals(exactQueryJsonA, exactQueryJsonB)) {
            Log.e(TAG, "testCreate() - exact query objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(exactQueryJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(exactQueryJsonB));
        }

        exactQueryJsonA = storageA.exactQuery("Id", "xxx");
        exactQueryJsonB = storageB.exactQuery("Id", "xxx");

        if (!equals(exactQueryJsonA, exactQueryJsonB)) {
            Log.e(TAG, "testCreate() - exact query 2 objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(exactQueryJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(exactQueryJsonB));
        }

        // === queryAll()

        JSONArray queryAllJsonA = storageA.queryAll("Id", "002q0000001ZjVMAA0");
        JSONArray queryAllJsonB = storageB.queryAll("Id", "002q0000001ZjVMAA0");

        if (!equals(queryAllJsonA, queryAllJsonB)) {
            Log.e(TAG, "testCreate() - query all objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(queryAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(queryAllJsonB));
        }

        queryAllJsonA = storageA.queryAll("Id", "xxx");
        queryAllJsonB = storageB.queryAll("Id", "xxx");

        if (!equals(queryAllJsonA, queryAllJsonB)) {
            Log.e(TAG, "testCreate() - query all 2 objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(queryAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(queryAllJsonB));
        }


        tearDown();
    }

    private void testDelete() throws JSONException {
        setup();

        // === create()

        JSONObject noteA1 = storageA.create(createTestNoteObject());
        JSONObject noteB1 = storageB.create(createTestNoteObject());
        JSONObject noteA2 = storageA.create(createTestNoteObject2());
        JSONObject noteB2 = storageB.create(createTestNoteObject2());
        JSONObject eventA1 = storageA.create(createTestEventObject());
        JSONObject eventB1 = storageB.create(createTestEventObject());
        JSONObject eventA2 = storageA.create(createTestEventObject2());
        JSONObject eventB2 = storageB.create(createTestEventObject2());

        // === fetchAllRecords()

        JSONArray fetchAllJsonA = storageA.fetchAllRecords();
        JSONArray fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testDelete() - fetch all objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        if (fetchAllJsonA.length() != 4 || fetchAllJsonB.length() != 4) {
            Log.e(TAG, "testDelete() - fetch all objects size is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " size: " + fetchAllJsonA.length());
            Log.e(TAG, storageB.getClass().getSimpleName() + " size: " + fetchAllJsonA.length());
        }

        // === delete()

        storageA.delete(eventA2.getLong("_soupEntryId"));
        storageB.delete(eventB2.getLong("_soupEntryId"));

        fetchAllJsonA = storageA.fetchAllRecords();
        fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testDelete() - fetch all objects 2 are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        if (fetchAllJsonA.length() != 3 || fetchAllJsonB.length() != 3) {
            Log.e(TAG, "testDelete() - fetch all objects 2 size is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " size: " + fetchAllJsonA.length());
            Log.e(TAG, storageB.getClass().getSimpleName() + " size: " + fetchAllJsonA.length());
        }

        // === deleteAll()

        int deletedA = storageA.deleteAll("Id", "002q0000001ZjVMAA0");
        int deletedB = storageB.deleteAll("Id", "002q0000001ZjVMAA0");

        if (deletedA != 2 || deletedB != 2) {
            Log.e(TAG, "testDelete() - number of deleted items is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " size: " + deletedA);
            Log.e(TAG, storageB.getClass().getSimpleName() + " size: " + deletedB);
        }

        fetchAllJsonA = storageA.fetchAllRecords();
        fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testDelete() - fetch all objects 3 are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        if (fetchAllJsonA.length() != 1 || fetchAllJsonB.length() != 1) {
            Log.e(TAG, "testDelete() - fetch all objects 3 size is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " size: " + fetchAllJsonA.length());
            Log.e(TAG, storageB.getClass().getSimpleName() + " size: " + fetchAllJsonA.length());
        }

        tearDown();
    }

    private void testUpdate() throws JSONException {
        setup();

        // === create()/upsert()

        JSONObject returnedJsonA1 = storageA.create(createTestNoteObject());
        JSONObject returnedJsonB1 = storageB.create(createTestNoteObject());
        JSONObject returnedJsonA2 = storageA.upsert(createTestNoteObject2());
        JSONObject returnedJsonB2 = storageB.upsert(createTestNoteObject2());

        if (!equals(returnedJsonA2, returnedJsonB2)) {
            Log.e(TAG, "testUpdate() - upserted objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(returnedJsonA2));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(returnedJsonB2));
        }

        // === fetchAllRecords()

        JSONArray fetchAllJsonA = storageA.fetchAllRecords();
        JSONArray fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testUpdate() - fetch all objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        // === update()

        returnedJsonA1.put("retryCount", 2);
        returnedJsonB1.put("retryCount", 2);

        JSONObject returnedUpdateA = storageA.update(returnedJsonA1, returnedJsonA1.getLong("_soupEntryId"));
        JSONObject returnedUpdateB = storageB.update(returnedJsonB1, returnedJsonB1.getLong("_soupEntryId"));

        if (!equals(returnedUpdateA, returnedUpdateB)) {
            Log.e(TAG, "testCreate() - updated objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(returnedUpdateA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(returnedUpdateB));
        }

        fetchAllJsonA = storageA.fetchAllRecords();
        fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testUpdate() - fetch all of updated objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        // === upsert()

        returnedJsonA1.put("retryCount", 3);
        returnedJsonB1.put("retryCount", 3);

        JSONObject returnedUpsertA = storageA.upsert(returnedJsonA1);
        JSONObject returnedUpsertB = storageB.upsert(returnedJsonB1);

        if (!equals(returnedUpsertA, returnedUpsertB)) {
            Log.e(TAG, "testCreate() - upserted objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(returnedUpsertA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(returnedUpsertB));
        }

        fetchAllJsonA = storageA.fetchAllRecords();
        fetchAllJsonB = storageB.fetchAllRecords();

        if (!equals(fetchAllJsonA, fetchAllJsonB)) {
            Log.e(TAG, "testUpdate() - fetch all of upserted objects are not equal");
            Log.e(TAG, storageA.getClass() + ":\n" + toPrettyString(fetchAllJsonA));
            Log.e(TAG, storageB.getClass() + ":\n" + toPrettyString(fetchAllJsonB));
        }

        tearDown();
    }

    private void testRecordsCount() throws JSONException {
        setup();

        int recordsCountA = storageA.getRecordsCount();
        int recordsCountB = storageB.getRecordsCount();

        if (recordsCountA != 0 || recordsCountB != 0) {
            Log.e(TAG, "testRecordsCount() - records count is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " count: " + recordsCountA);
            Log.e(TAG, storageB.getClass().getSimpleName() + " count: " + recordsCountB);
        }

        storageA.create(createTestNoteObject());
        storageB.create(createTestNoteObject());

        recordsCountA = storageA.getRecordsCount();
        recordsCountB = storageB.getRecordsCount();

        if (recordsCountA != 1 || recordsCountB != 1) {
            Log.e(TAG, "testRecordsCount() - records count 2 is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " count: " + recordsCountA);
            Log.e(TAG, storageB.getClass().getSimpleName() + " count: " + recordsCountB);
        }

        storageA.create(createTestNoteObject2());
        storageB.create(createTestNoteObject2());

        recordsCountA = storageA.getRecordsCount();
        recordsCountB = storageB.getRecordsCount();

        if (recordsCountA != 2 || recordsCountB != 2) {
            Log.e(TAG, "testRecordsCount() - records count 3 is incorrect");
            Log.e(TAG, storageA.getClass().getSimpleName() + " count: " + recordsCountA);
            Log.e(TAG, storageB.getClass().getSimpleName() + " count: " + recordsCountB);
        }



        tearDown();
    }

    private JSONObject createTestNoteObject() throws JSONException {
        JSONObject newObject = new JSONObject();
        newObject.put("Id", "002q0000001ZjVMAA0");
        newObject.put("objectType", "Note");
        newObject.put("operation", "CREATE");
        newObject.put("retryCount", 0);

        JSONObject fields = new JSONObject();
        fields.put("Body", "test body 1");
        fields.put("Title", "test title 1");
        newObject.put("fields", fields);
        return newObject;
    }

    private JSONObject createTestNoteObject2() throws JSONException {
        JSONObject newObject = new JSONObject();
        newObject.put("Id", "002q0000001ZjVMAA0");
        newObject.put("objectType", "Note");
        newObject.put("operation", "UPDATE");
        newObject.put("retryCount", 1);

        JSONObject fields = new JSONObject();
        fields.put("Body", "test body 2");
        newObject.put("fields", fields);
        return newObject;
    }

    private JSONObject createTestEventObject() throws JSONException {
        JSONObject newObject = new JSONObject();
        newObject.put("Id", "001q0000001ZjVMAA0");
        newObject.put("objectType", "Event");
        newObject.put("operation", "CREATE");
        newObject.put("retryCount", 1);

        JSONObject fields = new JSONObject();
        fields.put("EventDate", "12/12/2013");
        newObject.put("fields", fields);
        return newObject;
    }

    private JSONObject createTestEventObject2() throws JSONException {
        JSONObject newObject = new JSONObject();
        newObject.put("Id", "001q0000001ZjVMAA1");
        newObject.put("objectType", "Event");
        newObject.put("operation", "CREATE");
        newObject.put("retryCount", 1);

        JSONObject fields = new JSONObject();
        fields.put("EventDate", "12/12/2013");
        newObject.put("fields", fields);
        return newObject;
    }

    private void setup() {
        storageA.createStorage();
        storageB.createStorage();
    }

    private void tearDown() {
        storageA.deleteStorage();
        storageB.deleteStorage();
    }

    private String toString(JSONObject o) {
        return o == null ? "null" : o.toString();
    }

    private String toPrettyString(JSONObject o) {
        try {
            return o == null ? "null" : o.toString(2);
        } catch (JSONException e) {
            Log.w(TAG, e);
            return "null";
        }
    }

    private String toString(JSONArray o) {
        return o == null ? "null" : o.toString();
    }

    private String toPrettyString(JSONArray o) {
        try {
            return o == null ? "null" : o.toString(2);
        } catch (JSONException e) {
            Log.w(TAG, e);
            return "null";
        }
    }

    private JSONObject duplicate(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        try {
            return new JSONObject(jsonObject.toString());
        } catch (JSONException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    private JSONArray duplicate(JSONArray jsonObject) {
        if (jsonObject == null) return null;

        try {
            return new JSONArray(jsonObject.toString());
        } catch (JSONException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    private boolean equals(JSONObject o1, JSONObject o2) {
        if (o1 == o2) return true;
        if (o1 == null) return false;
        if (o2 == null) return false;

        JSONObject copy1 = duplicate(o1);
        JSONObject copy2 = duplicate(o2);

        if (copy1.isNull("_soupEntryId") != copy2.isNull("_soupEntryId") ||
                copy1.isNull("_soupLastModifiedDate") != copy2.isNull("_soupLastModifiedDate")) {
            return false;
        }
//        copy1.remove("_soupEntryId");
        copy1.remove("_soupLastModifiedDate");
//        copy2.remove("_soupEntryId");
        copy2.remove("_soupLastModifiedDate");

        return toString(copy1).equals(toString(copy2));
    }

    private boolean equals(JSONArray a1, JSONArray a2) {
        if (a1 == a2) return true;
        if (a1 == null) return false;
        if (a2 == null) return false;
        if (a1.length() != a2.length()) return false;

        for (int i = 0; i < a1.length(); i++) {
            Object object1 = a1.opt(i);
            Object object2 = a2.opt(i);

            if (object1 instanceof JSONObject && object2 instanceof JSONObject) {
                if (!equals((JSONObject) object1, (JSONObject) object2)) return false;
            }
            else {
                throw new IllegalStateException("Unimplemented equals for " + object1 + " and " + object2);
            }
        }

        return true;
    }
}
