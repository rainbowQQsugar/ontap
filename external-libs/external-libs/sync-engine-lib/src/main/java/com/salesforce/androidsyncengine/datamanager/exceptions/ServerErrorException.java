package com.salesforce.androidsyncengine.datamanager.exceptions;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subresponse;

import org.json.JSONObject;

/**
 * Created by Jakub Stefanowski on 03.11.2016.
 */
public class ServerErrorException extends SyncException {

    private static final String TAG = "ServerErrorException";

    private final int statusCode;

    private final String errorCode;

    private final String serverMessage;

    public static ServerErrorException createFrom(RestResponse response, Context context) {
        return createFrom(response, null, context);
    }

    public static ServerErrorException createFrom(RestResponse response, String additionalText, Context context) {
        JSONObject errorInfo = new JSONObject();
        try {
            errorInfo = response.asJSONArray().getJSONObject(0);
//            Log.i(TAG, "error response: " + response.asJSONArray().toString(2));
        }
        catch (Exception e) {
            Log.e(TAG, "Error while parsing error response.", e);
        }

        int statusCode = response.getStatusCode();
        String errorCode = errorInfo.optString("errorCode");
        String errorMessage = errorInfo.optString("message");
        String readableMessage = createReadableMessage(statusCode, errorCode, errorMessage);
        if (!TextUtils.isEmpty(additionalText)) {
            readableMessage += additionalText;
        }

        return new ServerErrorException(statusCode, errorCode, errorMessage, readableMessage);
    }

    public static ServerErrorException createFrom(Subresponse response, Context context) {
        return createFrom(response, null, context);
    }

    public static ServerErrorException createFrom(Subresponse response, String additionalText, Context context) {
        JSONObject errorInfo = new JSONObject();
        try {
            errorInfo = response.asJSONArray().getJSONObject(0);
        }
        catch (Exception e) {
            Log.e(TAG, "Error while parsing error response.", e);
        }

        int statusCode = response.getStatusCode();
        String errorCode = errorInfo.optString("errorCode");
        String errorMessage = errorInfo.optString("message");
        String readableMessage = createReadableMessage(statusCode, errorCode, errorMessage);
        if (!TextUtils.isEmpty(additionalText)) {
            readableMessage += additionalText;
        }

        return new ServerErrorException(statusCode, errorCode, errorMessage, readableMessage);
    }

    public static ServerErrorException createFrom(CompositeSubresponse response, Context context) {
        return createFrom(response, null, context);
    }

    public static ServerErrorException createFrom(CompositeSubresponse response, String additionalText, Context context) {
        JSONObject errorInfo = new JSONObject();
        try {
            errorInfo = response.asJSONArray().getJSONObject(0);
        }
        catch (Exception e) {
            Log.e(TAG, "Error while parsing error response.", e);
        }

        int statusCode = response.getStatusCode();
        String errorCode = errorInfo.optString("errorCode");
        String errorMessage = errorInfo.optString("message");
        String readableMessage = createReadableMessage(statusCode, errorCode, errorMessage);
        if (!TextUtils.isEmpty(additionalText)) {
            readableMessage += additionalText;
        }

        return new ServerErrorException(statusCode, errorCode, errorMessage, readableMessage);
    }

    private ServerErrorException(int statusCode, String errorCode, String serverMessage, String readableMessage) {
        super(readableMessage);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.serverMessage = serverMessage;
    }

    private static String createReadableMessage(int statusCode, String errorCode, String errorMessage) {
        if (!TextUtils.isEmpty(errorMessage)) {
            return "Server error - " + errorMessage;
        }
        else if (!TextUtils.isEmpty(errorCode)) {
            return "Server error - code " + errorCode;
        }
        else {
            return "Server error - status code " + statusCode;
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getServerMessage() {
        return serverMessage;
    }
}
