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

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsdk.phonegap.plugin.ForcePlugin;
import com.salesforce.androidsdk.phonegap.plugin.JavaScriptPluginVersion;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.Category__c;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.utils.CategoryUtils;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DataUtils;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * PhoneGap plugin
 */
public class DSAContentPlugin extends ForcePlugin {

    private static final String TAG="DSAContentPlugin";
    
    /**
     * Supported plugin actions that the client can take.
     */
    enum Action {
        getCategory,
        getCategoryContentArray,
        getContentPathFromSFID
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
                synchronized (DSAContentPlugin.class) {
                    try {
                        switch (action) {
                            case getCategory:
                                getCategory(args, callbackContext);
                                break;
                            case getContentPathFromSFID:
                                getContentPathFromSFID(args, callbackContext);
                                break;
                            default:
                                callbackContext.error(String.format("No handler for action %s", action));
                        }
                    } catch (Exception e) {
                            Log.w("DSAContentPlugin.execute", e.getMessage(), e);
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
    protected void getCategory(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "getCategory : " + args);
        try {

            JSONArray categoryPathArray = (JSONArray) ((JSONObject) (args.get(0))).get("categoryPathName");
            String categoryPathName = categoryPathArray.getString(0);

            Log.e(TAG, "categoryPathName: " + categoryPathName);

            String[] categoryArray = categoryPathName.split(",");

            int length = categoryArray.length;

            String searchCategory;

            if (length > 1) {
                searchCategory = categoryArray[length - 1];
            } else {
                searchCategory = categoryArray[0];
            }

            Log.e(TAG, "searchCategory: " + searchCategory);

            Context context = cordova.getActivity();

            String categoryName = searchCategory;
            String smartSqlFilter = String.format("{Category__c:%s} = '%s' ",
                    SyncEngineConstants.StdFields.NAME,
                    categoryName);

            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CATEGORY, smartSqlFilter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

            // TODO: Match subcategory with provided parent category

            if (recordsArray.length() > 0) {
                JSONObject jsonObject2 = recordsArray.getJSONArray(0).getJSONObject(0);
                Category__c category = new Category__c(jsonObject2);

//                List<Category__c> subCategories = CategoryUtils.getSubCategories(category.getId());

                JSONArray subcategoryJSON= new JSONArray();
//                for (Category__c subCategory : subCategories) {
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("Name", subCategory.getName());
//                    subcategoryJSON.put(jsonObject);
//                }
                List<ContentVersion> contentVersions = DataUtils.fetchContentVersionsForCategory(category, context);

                JSONArray contentJSON= new JSONArray();
                for (ContentVersion contentVersion : contentVersions) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("SFDCID", contentVersion.getId());

                    String uri = contentVersion.getContentUrl();
                    if (uri == null || uri.trim().equals("") || uri.trim().equals("null")) {
                        jsonObject.put("URI", contentVersion.getCompleteFileName(context));
                    } else {
                        jsonObject.put("URI", contentVersion.getContentUrl());
                    }
                    jsonObject.put("Type", contentVersion.getFileType());
                    jsonObject.put("Name", contentVersion.getTitle());

                    contentJSON.put(jsonObject);
                }

                JSONObject data = new JSONObject();
                data.put("Name", category.getName());
                data.put("sub-categories", subcategoryJSON);
                data.put("content", contentJSON);

                Log.e(TAG, "data: " + data);
                callbackContext.success(data);
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * @param callbackContext Used when calling back into Javascript.
     * @throws JSONException
     */
    protected void getContentPathFromSFID(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "getContentPathFromSFID : " + args);
        try {

            Context context = cordova.getActivity();

            String salesforceID = (String) ((JSONObject) (args.get(0))).get("salesforceID");

            String data = DataManagerFactory.getDataManager().getFilePath(context, "ContentVersion", salesforceID);

            if (data == null) {
                data = DataManagerFactory.getDataManager().getFilePath(context, "Attachment", salesforceID);
            }



            // test code
            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_ALL_FORMAT, "RecordType");

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

            Log.e("Babu", "RecordType size is: " + recordsArray.length());

            int length = recordsArray.length();
            for (int i=0; i< length; i++) {
                JSONObject jsonObject2 = recordsArray.getJSONArray(i).getJSONObject(0);
                Log.e("Babu", "record : " + jsonObject2);
            }

            Log.e(TAG, "data: " + data);
            callbackContext.success(data);

        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

}
