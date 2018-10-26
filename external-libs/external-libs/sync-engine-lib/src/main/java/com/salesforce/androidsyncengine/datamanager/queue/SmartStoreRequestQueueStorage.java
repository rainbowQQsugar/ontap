package com.salesforce.androidsyncengine.datamanager.queue;

import android.util.Log;

import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.salesforce.androidsyncengine.datamanager.DataManager.CONSTANTS_ID;

/**
 * Created by Jakub Stefanowski on 16.01.2017.
 */

public class SmartStoreRequestQueueStorage implements RequestQueueStorage {

    private static final String TAG = "SmartStoreRequestQueue";

    private static final String OPERATION = "operation";
    private static final String OBJECT_TYPE = "objectType";

    private static String QUEUE_SOUP = "Queue";

    private static IndexSpec[] QUEUE_INDEX_SPEC = {
            new IndexSpec(CONSTANTS_ID, SmartStore.Type.string),
            new IndexSpec(OBJECT_TYPE, SmartStore.Type.string),
            new IndexSpec(OPERATION, SmartStore.Type.string)
    };

    private SmartStore smartStore;

    public SmartStoreRequestQueueStorage(SmartStore smartStore) {
        this.smartStore = smartStore;
    }

    @Override
    public void createStorage() {
        smartStore.registerSoup(QUEUE_SOUP, QUEUE_INDEX_SPEC);
    }

    @Override
    public JSONObject get(int position) {
        throw new UnsupportedOperationException("Method is not yet implemented");
    }

    @Override
    public void deleteStorage() {
        if (smartStore.hasSoup(QUEUE_SOUP)) {
            // TODO: if there are any operations in queue, hold off until they are all performed
            smartStore.dropSoup(QUEUE_SOUP);
        }
    }

    @Override
    public JSONObject create(JSONObject data) throws JSONException {
        return smartStore.create(QUEUE_SOUP, data);
    }

    @Override
    public JSONObject upsert(JSONObject data) throws JSONException {
        return smartStore.upsert(QUEUE_SOUP, data);
    }

    @Override
    public JSONArray retrieve(Long... soupEntryIds) throws JSONException {
        return smartStore.retrieve(QUEUE_SOUP, soupEntryIds);
    }

    @Override
    public void delete(Long... soupEntryIds) {
        smartStore.delete(QUEUE_SOUP, soupEntryIds);
    }

    @Override
    public JSONObject update(JSONObject soupElt, long soupEntryId) throws JSONException {
        return smartStore.update(QUEUE_SOUP, soupElt, soupEntryId);
    }

    @Override
    public JSONArray fetchAllRecords() {
        Log.i(TAG, "In fetchAllRecords for: " + QUEUE_SOUP);
        JSONArray result = null;
        QuerySpec querySpec = QuerySpec.buildRangeQuerySpec(QUEUE_SOUP, null, null, null, null,
                QuerySpec.Order.ascending, 10);
        int count = smartStore.countQuery(querySpec);
        Log.i(TAG, "In fetchAllRecords for: " + QUEUE_SOUP + " count: " + count);

        querySpec = QuerySpec.buildRangeQuerySpec(QUEUE_SOUP, null, null, null, null,
                QuerySpec.Order.ascending, count);
        try {
            result = smartStore.query(querySpec, 0);
        }
        catch (JSONException e) {
            Log.e(TAG, "Error occurred while attempting to run query. Please verify validity of " +
                    "the query.", e);
        }
        return result;
    }

    @Override
    public int deleteAll(String path, String exactMatchKey) throws JSONException {
        QuerySpec querySpec = QuerySpec.buildExactQuerySpec(
                QUEUE_SOUP, path, exactMatchKey, null, QuerySpec.Order.ascending, 10);
        int count = smartStore.countQuery(querySpec);
        int deleted = 0;
        Log.i(TAG, "queue match count is: " + count);

        while (count > 0) {
            JSONArray result = smartStore.query(querySpec, 0);
            Log.i(TAG, "result count is: " + result.length());
            if (result.length() == 0) {
                break;
            }
            else {
                int recordCount = result.length();
                for (int j = 0; j < recordCount; j++) {
                    JSONObject recordObject = result.getJSONObject(j);
                    Log.i(TAG, "in deleteQueueRecordFromClient. queue record:" + recordObject.toString());
                    QueueObject temp = new QueueObject(recordObject);
                    delete(temp.getSoupEntryId());
                    deleted++;
                }
            }
            count = smartStore.countQuery(querySpec);
        }
        return deleted;
    }

    @Override
    public JSONObject exactQuery(String path, String exactMatchKey) {
        try {
            if (exactMatchKey == null) return null;

            QuerySpec querySpec = QuerySpec.buildExactQuerySpec(QUEUE_SOUP, path,
                    exactMatchKey, null, QuerySpec.Order.ascending, 10);
            JSONArray result = smartStore.query(querySpec, 0);
            if (result.length() == 0) {
                return null;
            }
            else {
                return result.getJSONObject(0);
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "error in exactQuery for: " + QUEUE_SOUP + " path: " + path
                    + " exactMatchKey:" + exactMatchKey, e);
        }
        return null;
    }

    @Override
    public JSONArray queryAll(String path, String exactMatchKey) {
        Log.i(TAG, "In queryAll for: " + QUEUE_SOUP);
        JSONArray result = null;
        QuerySpec querySpec = QuerySpec.buildRangeQuerySpec(QUEUE_SOUP, path, exactMatchKey, null,
                null, QuerySpec.Order.ascending, 10);
        int count = smartStore.countQuery(querySpec);
        Log.i(TAG, "In queryAll for: " + QUEUE_SOUP + " count: " + count);
        querySpec = QuerySpec.buildRangeQuerySpec(QUEUE_SOUP, path, exactMatchKey, null, null,
                QuerySpec.Order.ascending, count);

        try {
            result = smartStore.query(querySpec, 0);
        }
        catch (JSONException e) {
            Log.e(TAG, "Error occurred while attempting to run query. Please verify validity of the query.", e);
        }

        return result;
    }

    @Override
    public boolean isSingleUserStorage() {
        return true;
    }

    @Override
    public int getRecordsCount() {
        QuerySpec querySpec = QuerySpec.buildRangeQuerySpec(QUEUE_SOUP, "Id", null, null, null, QuerySpec.Order.ascending, 10);
        int count = smartStore.countQuery(querySpec);
        Log.i(TAG, "In getCountForQuery for: " + QUEUE_SOUP + " count: " + count);
        return count;
    }
}
