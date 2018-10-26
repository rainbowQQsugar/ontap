package com.salesforce.androidsyncengine.datamanager.exceptions;

import android.content.Context;

/**
 * Created by Jakub Stefanowski on 08.11.2016.
 */
public class UnsupportedEncodingSyncException extends SyncException {

    public UnsupportedEncodingSyncException(Throwable e, String message) {
        super(message, e);
    }
}
