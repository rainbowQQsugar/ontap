package com.salesforce.androidsyncengine.datamanager.exceptions;

import android.content.Context;

import com.salesforce.androidsyncengine.R;

/**
 * Created by Jakub Stefanowski on 03.11.2016.
 */
public class ConnectionLostException extends SyncException {

    public static ConnectionLostException create(Throwable e, Context c) {
        String errorMessage = e.getMessage();
        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("token")) {
                return new ConnectionLostException(errorMessage, e);
            }
        }
        return new ConnectionLostException(c.getString(R.string.sync_failure_message), e);
    }

    private ConnectionLostException(String message, Throwable e) {
        super(message, e);
    }
}
