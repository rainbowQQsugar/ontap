package com.salesforce.androidsyncengine.syncsteps;


import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

import rx.Observable;

/**
 * Simple implementation of {@link ObservableSyncStep}.
 *
 * Created by Jakub Stefanowski on 11.10.2017.
 */

public final class BasicObservableSyncStep extends ObservableSyncStep {

    private Observable<?> observable;

    public BasicObservableSyncStep(Observable<?> observable) {
        this.observable = observable;
    }

    public BasicObservableSyncStep() {
    }

    public BasicObservableSyncStep setObservable(Observable<?> observable) {
        this.observable = observable;
        return this;
    }

    @Override
    protected Observable<?> createObservable(SyncControls syncControls) throws SyncException {
        return observable;
    }
}
