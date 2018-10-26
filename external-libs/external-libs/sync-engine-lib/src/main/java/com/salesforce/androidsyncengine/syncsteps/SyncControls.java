package com.salesforce.androidsyncengine.syncsteps;

import android.content.Context;

import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;

import java.util.Set;

/**
 * Interface for accessing SyncEngine from SyncStep.
 *
 * Created by Jakub Stefanowski on 13.09.2017.
 */
public interface SyncControls {
    Context getContext();
    ManifestProcessor getManifestProcessor();
    SyncHelper getSyncHelper();
    SyncStatus getStatus();
    String getApiVersion();
    SmartStoreDataManagerImpl getDataManager();
    DownloadHelper getDownloadHelper();
    Set<String> getObjectsToFetchDuringAutoSync();
}
