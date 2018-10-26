package com.salesforce.androidsyncengine.datamanager.exceptions;

/**
 * Created by Jakub Stefanowski on 04.11.2016.
 */
public class SimpleSyncException extends SyncException {


    public SimpleSyncException(Throwable e, String detailMessage) {
        super(detailMessage, e);
    }

    public SimpleSyncException(Throwable e) {
        super(e.getMessage(), e);
    }
}
