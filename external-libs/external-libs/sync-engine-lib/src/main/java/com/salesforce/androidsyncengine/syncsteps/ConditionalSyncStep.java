package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

/**
 * Class that wraps another Sync Step and executes it only if provided conditions are met.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */

public class ConditionalSyncStep extends SyncStepWrapper {

    public interface Condition {
        /** Check if conditions are met. */
        boolean accept(SyncControls syncControls) throws SyncException;
    }

    private SyncStep syncStep;

    private Condition condition;

    public ConditionalSyncStep setSyncStep(SyncStep syncStep) {
        this.syncStep = syncStep;
        return this;
    }

    public ConditionalSyncStep setCondition(Condition condition) {
        this.condition = condition;
        return this;
    }

    @Override
    protected void preExecute(SyncControls syncControls) throws SyncException {
        super.preExecute(syncControls);
        if (syncStep == null) throw new IllegalStateException("Sync step cannot be null");

        if (condition == null || condition.accept(syncControls)) {
            wrapSyncStep(syncStep);
        }
        else {
            wrapSyncStep(EmptySyncStep.INSTANCE);
        }
    }
}
