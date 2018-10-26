package com.salesforce.androidsyncengine.datamanager.exceptions;

/**
 * Runtime wrapper for regular sync exception.
 *
 * Created by Jakub Stefanowski on 12.10.2017.
 */
public class RuntimeSyncException extends RuntimeException {

    public static RuntimeSyncException wrap(SyncException e) {
        return new RuntimeSyncException(e);
    }

    private RuntimeSyncException(SyncException e) {
        super(e);
    }

    public SyncException getSyncException() {
        return (SyncException) getCause();
    }
}
