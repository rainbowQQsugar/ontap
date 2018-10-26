package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

import rx.Observable;

/**
 * Sync step that uses Observable to run the synchronization. It catches all Runtime exceptions and
 * if possible converts them to SyncException.
 *
 * Created by Jakub Stefanowski on 11.10.2017.
 */
public abstract class ObservableSyncStep implements SyncStep {

    @Override
    public final void execute(SyncControls syncControls) throws SyncException {
        try {
            createObservable(syncControls).subscribe();
        }
        catch (RuntimeException e) {
            handleRuntimeException(e);
        }
    }

    private void handleRuntimeException(RuntimeException e) throws SyncException {
        Throwable cause = e.getCause();
        if (cause instanceof SyncException) {
            throw (SyncException) cause;
        }
        else {
            throw e;
        }
    }

    protected abstract Observable<?> createObservable(SyncControls syncControls) throws SyncException;
}
