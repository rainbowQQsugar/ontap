/*
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dsa.phonegap;

import android.util.Log;

import com.salesforce.androidsdk.phonegap.plugin.ForcePlugin;
import com.salesforce.androidsdk.phonegap.plugin.JavaScriptPluginVersion;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * PhoneGap plugin
 */
public class DSASynchronizedDataPlugin extends ForcePlugin {

    private static final String TAG="DSASynchronizedDataPlugin";
    
    /**
     * Supported plugin actions that the client can take.
     */
    enum Action {
        get,
        search,
        upsert
    }

    @Override
    public boolean execute(String actionStr, JavaScriptPluginVersion jsVersion,
                           final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        final long start = System.currentTimeMillis();

        // Figure out action
        final Action action;
        try {
            action = Action.valueOf(actionStr);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unknown action " + actionStr);
            return false;
        }

        // Not running smartstore action on the main thread
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                // All actions need to be serialized
                // Overkill right now ... remove after testing
                synchronized (DSASynchronizedDataPlugin.class) {
                    try {
                        switch (action) {
                            case get:
                                get(args, callbackContext);
                                break;
                            case search:
                                search(args, callbackContext);
                                break;
                            case upsert:
                                upsert(args, callbackContext);
                                break;
                            default:
                                callbackContext.error(String.format("No handler for action %s", action));
                        }
                    } catch (Exception e) {
                            Log.w(TAG, e.getMessage(), e);
                            callbackContext.error(e.getMessage());
                        }
                        Log.d(TAG, "Total time for " + action + "->" + (System.currentTimeMillis() - start));
                    }
                }
            });

        Log.d(TAG, "Main thread time for " + action + "->" + (System.currentTimeMillis() - start));
        return true;
    }

    /**
     * @param callbackContext Used when calling back into Javascript.
     * @throws JSONException
     */
    protected void get(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "get" + args);
        try {

            JSONArray salesforceIDArray = (JSONArray) ((JSONObject) (args.get(0))).get("salesforceID");

            JSONObject jo = new JSONObject();

            JSONArray ja = new JSONArray();

            for (int i = 0; i < salesforceIDArray.length(); i++) {
                String salesforceID = salesforceIDArray.getString(i);

                Log.i(TAG, "querying for id: " + salesforceID);

                if (salesforceID.trim().length() != 18) {
                    callbackContext.error(salesforceID + " Id has to be 18 characters");
                    break;
                }

                SmartStore smartStore = DataManagerFactory.getDataManager().getSmartStore();

                List<String> soupNames = smartStore.getAllSoupNames();

                Log.e("Babu", "soupNames: " + soupNames);

                String smartSqlFilter;
                String smartSql;

                for (String soupName : soupNames) {
                    smartSqlFilter = String.format("{%s:%s} = '%s'", soupName, SyncEngineConstants.StdFields.ID, salesforceID);
                    smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, soupName, smartSqlFilter);
                    JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
                    if (recordsArray.length() > 0) {
                        ja.put(recordsArray.getJSONArray(0)
                                .getJSONObject(0));
                        break;
                    }
                }

            }
            
            jo.put("objectArray", ja);
            callbackContext.success(jo);
            return;

        } catch (Exception e) {
            Log.e(TAG, "exception : " + e.getMessage());
            e.printStackTrace();
            callbackContext.error(e.getMessage());

        }

    }

    /**
     * @param callbackContext Used when calling back into Javascript.
     * @throws JSONException
     */
    protected void search(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "search: " + args);
        try {

            JSONObject jo = new JSONObject();

            JSONArray ja = new JSONArray();

            JSONObject searchObject = ((JSONObject) (args.get(0)));
            String objectType = searchObject.getString("objectType");
            String searchField = searchObject.getString("searchField");
            String searchString = searchObject.getString("searchString").trim();

            SmartStore smartStore = DataManagerFactory.getDataManager().getSmartStore();

            if (!smartStore.hasSoup(objectType)) {
                callbackContext.error(objectType + " does not exist! ");
                return;
            }

            String smartSqlFilter;
            String smartSql;

            boolean useSmartSql = false;
            JSONArray recordsArray;

            if (searchField.trim().equals("")) {
                // return all records
                recordsArray = DataManagerFactory.getDataManager().fetchAllRecords(objectType, SyncEngineConstants.StdFields.ID);
            } else {
                DataManager dataManager = DataManagerFactory.getDataManager();
                String updatedSearchString = DataManagerFactory.getDataManager().getSalesforceIdFromTemporaryId(searchString);
                if (updatedSearchString != null) {
                    Log.i(TAG, "*** using updated value ***");
                    searchString = updatedSearchString;
                }

                smartSqlFilter = String.format("{%s:%s} = '%s'", objectType, searchField, searchString);
                smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, objectType, smartSqlFilter);
                Log.i(TAG, "smartSql: " + smartSql);
                useSmartSql = true;
                recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            }

            Log.i(TAG, "length: " + recordsArray.length());

            if (recordsArray.length() > 0) {
                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject jsonObject = !useSmartSql ? recordsArray.getJSONObject(i) : recordsArray.getJSONArray(i).getJSONObject(0);
                    ja.put(jsonObject);
                }
            } else {
                callbackContext.error("No objects found!");
            }

            jo.put("objectArray", ja);
            callbackContext.success(jo);
            return;

        }
        catch (Exception e) {
            Log.e(TAG, "Exception : " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * @param callbackContext Used when calling back into Javascript.
     * @throws JSONException
     */
    protected void upsert(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "upsert: " + args);
        String id = null;
        try {

            JSONObject upsertObject = ((JSONObject) (args.get(0)));

            String objectType = upsertObject.getString("objectType");

            JSONArray objectArray = upsertObject.getJSONArray("objectArray");

            JSONObject objectData = objectArray.getJSONObject(0);



            Log.e(TAG, "upsertObject: " + upsertObject);

            Log.e(TAG, "objectData: " + objectData);

            id = objectData.getString("Id");

            if (id == null) {
                callbackContext.error("Id cannot be null. Got null Id!");
                return;
            }

            if (id.equalsIgnoreCase("NEW")) {
                objectData.remove("Id");
                Log.e(TAG, "objectData: " + objectData);
                // create new record
                id = DataManagerFactory.getDataManager().createRecord(objectType, objectData);
                 if (id == null) {
                     callbackContext.error("Error creating new record");
                 } else {
                     Log.i(TAG, "Created New Id: " + id);
                 }

            } else {
                boolean isSuccess = DataManagerFactory.getDataManager().updateRecord(objectType, id, objectData);
                if (isSuccess) {
                    Log.i(TAG, "successfully added: " + objectData);
                } else {
                    callbackContext.error("Error inserting record");
                    return;
                }
            }

        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

        JSONObject data = new JSONObject();
        data.put("success", "true");
        if (id != null) {
            data.put("Id", id);
        }
        callbackContext.success(data);
    }
}
