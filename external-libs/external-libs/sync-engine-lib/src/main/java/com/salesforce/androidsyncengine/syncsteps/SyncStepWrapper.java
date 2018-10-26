package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

/**
 * Created by Jakub Stefanowski on 09.10.2017.
 */

public abstract class SyncStepWrapper implements SyncStep {

    private SyncStep wrappedStep;

    protected void wrapSyncStep(SyncStep syncStep) {
        wrappedStep = syncStep;
    }

    @Override
    public final void execute(SyncControls syncControls) throws SyncException {
        preExecute(syncControls);
        if (wrappedStep == null) {
            throw new IllegalStateException("Wrapped step cannot be null");
        }
        else {
            wrappedStep.execute(syncControls);
        }
        postExecute(syncControls);
    }

    protected void preExecute(SyncControls syncControls) throws SyncException {

    }

    protected void postExecute(SyncControls syncControls) throws SyncException {

    }
}
