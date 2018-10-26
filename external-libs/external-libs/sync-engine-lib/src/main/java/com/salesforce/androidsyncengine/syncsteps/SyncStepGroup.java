package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Jakub Stefanowski on 14.09.2017.
 */

public class SyncStepGroup implements SyncStep {

    private final ArrayList<SyncStep> steps = new ArrayList<>();

    public SyncStepGroup() {}

    public SyncStepGroup(SyncStep... syncSteps) {
        this();
        add(syncSteps);
    }

    public SyncStepGroup(Collection<SyncStep> syncSteps) {
        this();
        add(syncSteps);
    }

    @Override
    public void execute(SyncControls syncControls) throws SyncException {
        for (SyncStep syncStep : steps) {
            syncStep.execute(syncControls);
        }
    }

    public SyncStepGroup add(SyncStep syncStep) {
        steps.add(syncStep);
        return this;
    }

    public SyncStepGroup add(SyncStep... syncSteps) {
        Collections.addAll(steps, syncSteps);
        return this;
    }

    public SyncStepGroup add(Collection<SyncStep> steps) {
        this.steps.addAll(steps);
        return this;
    }
}
