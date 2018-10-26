package com.salesforce.androidsyncengine.syncsteps.localcleanup;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.syncsteps.BasicObservableSyncStep;
import com.salesforce.androidsyncengine.syncsteps.ConditionalSyncStep;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.syncsteps.SyncStepWrapper;
import com.salesforce.androidsyncengine.syncsteps.objectprovider.OrderedObjectsProvider;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.util.List;

import rx.Observable;

/**
 * Class responsible for running local cleanup for all objects.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
public class LocalCleanupSyncStep extends SyncStepWrapper {

    private long startTime;

    @Override
    protected void preExecute(final SyncControls syncControls) throws SyncException {
        super.preExecute(syncControls);

        wrapSyncStep(new ConditionalSyncStep()
            .setCondition(new LocalCleanupCondition(syncControls))
            .setSyncStep(new BasicObservableSyncStep(createObservable(syncControls))));
    }

    private Observable<?> createObservable(final SyncControls syncControls) throws SyncException {
        List<String> objectNames = new OrderedObjectsProvider().getObjects(syncControls);
        final int objCount = objectNames.size();

        return Observable.from(objectNames)

                // Change initial sync status.
                .doOnSubscribe(() -> {
                    startTime = System.currentTimeMillis();
                    SyncStatus syncStatus = syncControls.getStatus();
                    syncStatus.setStage(SyncStatus.SyncStatusStage.RUN_LOCAL_CLEANUP);
                })

                // Log sync progress.
                .zipWith(Observable.range(0, objCount), (objectName, index) -> {
                    SyncStatus syncStatus = syncControls.getStatus();
                    syncStatus.setCurrentItem(index);
                    syncStatus.setTotalCount(objCount);
                    syncStatus.setDescription("Running cleanup on local object: " + objectName);
                    return objectName;
                })

                // Filter out specific objects.
                .filter(new LocalCleanupFilter(syncControls))

                // Run cleanup for each object.
                .doOnNext(new LocalCleanupConsumer(syncControls))

                // Log cleanup start time.
                .doOnCompleted(() -> PreferenceUtils.putLastLocalCleanup(startTime, syncControls.getContext()));
    }
}
