package com.salesforce.androidsyncengine.syncsteps.fetchdeleted;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.syncsteps.BasicObservableSyncStep;
import com.salesforce.androidsyncengine.syncsteps.ConditionalSyncStep;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.syncsteps.SyncStepWrapper;
import com.salesforce.androidsyncengine.syncsteps.objectprovider.OrderedObjectsProvider;

import java.util.List;

import rx.Observable;

/**
 * Sync step responsible for fetching deleted objects and removing them locally.
 *
 * Created by Jakub Stefanowski on 13.09.2017.
 */
public class FetchDeletedSyncStep extends SyncStepWrapper {

    @Override
    protected void preExecute(final SyncControls syncControls) throws SyncException {
        super.preExecute(syncControls);

        wrapSyncStep(new ConditionalSyncStep()
            .setCondition(new FetchDeletedCondition(syncControls))
            .setSyncStep(new BasicObservableSyncStep(createObservable(syncControls))));
    }

    private Observable<?> createObservable(final SyncControls syncControls) throws SyncException {
        List<String> objectNames = new OrderedObjectsProvider().getObjects(syncControls);
        final int objCount = objectNames.size();

        return Observable.from(objectNames)

                // Change initial sync status.
                .doOnSubscribe(() -> {
                    SyncStatus syncStatus = syncControls.getStatus();
                    syncStatus.setStage(SyncStatus.SyncStatusStage.GET_DELETED);
                })

                // Update sync progress.
                .zipWith(Observable.range(0, objCount), (objectName, index) -> {
                    SyncStatus syncStatus = syncControls.getStatus();
                    syncStatus.setCurrentItem(index);
                    syncStatus.setTotalCount(objCount);
                    syncStatus.setDescription("Fetching list of deleted records for object: " + objectName);
                    return objectName;
                })

                // Filter on which objects we need to do the fetch.
                .filter(new FetchDeletedFilter(syncControls))

                // Fetch and delete specific objects
                .compose(new FetchDeletedConsumer(syncControls));
    }
}
