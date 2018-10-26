package com.salesforce.androidsyncengine.datamanager.queue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jakub Stefanowski on 16.01.2017.
 */

public interface RequestQueueStorage {

    /** Call to create storage. */
    void createStorage();

    /** Call to delete all data from storage. */
    void deleteStorage();

    /** Create new record from JSON data. */
    JSONObject create(JSONObject data) throws JSONException;

    /** Create or update record from JSON data. */
    JSONObject upsert(JSONObject data) throws JSONException;

    /** Update record from JSON data. */
    JSONObject update(JSONObject data, long soupEntryId) throws JSONException;

    /** Get records by their soup ids. */
    JSONArray retrieve(Long... soupEntryIds) throws JSONException;

    /** Get record by its position. */
    JSONObject get(int position);

    /** Get all records. */
    JSONArray fetchAllRecords();

    /** Get number of all records. */
    int getRecordsCount();

    /** Get specific record. */
    JSONObject exactQuery(String path, String exactMatchKey);

    /** Run query to receive specific records. */
    JSONArray queryAll(String path, String exactMatchKey);

    /** Delete all matching records. */
    int deleteAll(String path, String exactMatchKey) throws JSONException;

    /** Delete records by their soup ids. */
    void delete(Long... soupEntryIds);

    /** Return true if the storage contains records only for one user. */
    boolean isSingleUserStorage();
}
