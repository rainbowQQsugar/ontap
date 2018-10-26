package com.salesforce.androidsyncengine.datamanager.synchelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.exceptions.ConnectionLostException;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * BatchResponseHandler created specifically for fetch requests. It can detect if there is another
 * page with more data, download it and provide as JSONArray.
 *
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public abstract class FetchResponseHandler implements BatchResponseHandler {
    private static final String TAG = "FetchResponseHandler";

    private static final int LARGE_RESPONSE_SIZE = 300;

    private static final String PARAM_RECORDS = "records";
    private static final String PARAM_NEXT_RECORDS_URL = "nextRecordsUrl";
    private static final String PARAM_TOTAL_SIZE = "totalSize";

    private final SyncHelper syncHelper;
    private final Context context;

    private int recordsHandled = 0;

    protected FetchResponseHandler(Context context, SyncHelper syncHelper) {
        this.syncHelper = syncHelper;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public Subrequest handleResponse(RestRequest originalRequest, RestResponse originalResponse, Subrequest subrequest, Subresponse subresponse) throws SyncException {
        Subrequest followingRequest = null;

        if (SyncHelper.LOG_REQUESTS) {
            Log.i(TAG, "subrequest: " + subrequest.getUrl());
        }

        try {
            if (subresponse.isSuccess()) {
                JSONObject jsonResponse = subresponse.asJSONObject();
                if (SyncHelper.LOG_REQUESTS) {
                    Log.i(TAG, "subresponse: " + jsonResponse.toString(2));
                }

                JSONArray records = getRecords(jsonResponse);
                String nextRecordsUrl = getNextRecordsUrl(jsonResponse);
                int totalSize = getTotalSize(jsonResponse);
                boolean isLast = TextUtils.isEmpty(nextRecordsUrl);
                handleRecords(records, recordsHandled, isLast);
                recordsHandled += records.length();

                if (!isLast) {
                    if (isLargeResponse(totalSize)) {
                        // If fetch response has large amount of total items fetch for next records here.
                        handleNextPages(originalRequest, nextRecordsUrl);
                    } else {
                        // If fetch response has a few items return following request.
                        followingRequest = syncHelper.getRawSubrequest(nextRecordsUrl);
                        followingRequest.setResponseHandler(this);
                    }
                }
            } else {
                if (SyncHelper.LOG_REQUESTS) {
                    Log.i(TAG, "subresponse: " + subresponse.asJSONArray().toString(2));
                }
                handleError(subrequest, subresponse);
            }
        } catch (JSONException e) {
            throw new SimpleSyncException(e, "Unable to read json subresponse.");
        }

        return followingRequest;
    }

    private void handleNextPages(RestRequest originalRequest, String nextRecordsUrl) throws SyncException {
        try {
            while (!TextUtils.isEmpty(nextRecordsUrl)) {
                Log.i(TAG, "Trying to fetch records from next page: " + nextRecordsUrl);
                RestRequest request = new RestRequest(RestRequest.RestMethod.GET, nextRecordsUrl, originalRequest.getAdditionalHttpHeaders());
                RestResponse response = syncHelper.sendRawRequest(request);

                if (response.isSuccess()) {
                    JSONObject jsonResponse = response.asJSONObject();
                    JSONArray records = getRecords(jsonResponse);
                    nextRecordsUrl = getNextRecordsUrl(jsonResponse);

                    boolean isLast = TextUtils.isEmpty(nextRecordsUrl);
                    handleRecords(records, recordsHandled, isLast);
                    recordsHandled += records.length();
                } else {
                    throw ServerErrorException.createFrom(response, context);
                }
            }
        } catch (IOException e) {
            throw ConnectionLostException.create(e, context);
        } catch (JSONException e) {
            throw new SimpleSyncException(e, "Unable to read json response.");
        }
    }

    protected String getNextRecordsUrl(JSONObject jsonResponse) throws JSONException {
        return jsonResponse.optString(PARAM_NEXT_RECORDS_URL, null);
    }

    protected int getTotalSize(JSONObject jsonResponse) throws JSONException {
        return jsonResponse.getInt(PARAM_TOTAL_SIZE);
    }

    protected JSONArray getRecords(JSONObject jsonResponse) throws JSONException {
        return jsonResponse.getJSONArray(PARAM_RECORDS);
    }

    protected void handleError(Subrequest subrequest, Subresponse subresponse) throws SyncException {
        throw ServerErrorException.createFrom(subresponse, context);
    }

    /** Small responses are batched together, large responses are sent separately. */
    protected boolean isLargeResponse(int totalSize) {
        return totalSize >= LARGE_RESPONSE_SIZE;
    }

    protected abstract void handleRecords(JSONArray records, int startIndex, boolean isLast) throws SyncException;
}
