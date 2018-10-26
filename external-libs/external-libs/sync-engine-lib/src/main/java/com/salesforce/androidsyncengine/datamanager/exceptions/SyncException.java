package com.salesforce.androidsyncengine.datamanager.exceptions;

import android.content.Context;

/**
 * Created by Jakub Stefanowski on 03.11.2016.
 */
public abstract class SyncException extends Exception {
    public SyncException() {
    }

    public SyncException(String detailMessage) {
        super(detailMessage);
    }

    public SyncException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SyncException(Throwable throwable) {
        super(throwable);
    }
}
