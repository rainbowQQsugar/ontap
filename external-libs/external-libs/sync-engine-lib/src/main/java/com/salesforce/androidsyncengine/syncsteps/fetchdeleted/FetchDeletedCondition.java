package com.salesforce.androidsyncengine.syncsteps.fetchdeleted;

import android.content.Context;
import android.text.TextUtils;

import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.syncsteps.ConditionalSyncStep;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Checks if {@link FetchDeletedSyncStep} should be executed.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
class FetchDeletedCondition implements ConditionalSyncStep.Condition {

    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US); // Mon, 14 Apr 2014 21:40:15 GMT

    private final Context context;

    private final ManifestProcessor manifestProcessor;

    public FetchDeletedCondition(SyncControls syncControls) {
        this.context = syncControls.getContext();
        this.manifestProcessor = syncControls.getManifestProcessor();
    }

    @Override
    public boolean accept(SyncControls syncControls) throws SyncException {
        Configuration configuration = PreferenceUtils.getConfiguration(context);
        boolean shouldFetchDeletedRecords;

        try {
            // Check if fetching deleted records should be executed.
            String expression = configuration.getShouldFetchDeletedRecords();
            if (TextUtils.isEmpty(expression)) {
                shouldFetchDeletedRecords = true;
            }
            else {
                HashMap<String, Object> variables = new HashMap<>();
                long lastTime = PreferenceUtils.getLastFetchDeletedRecords(context);
                variables.put("lastFetch", new DateHelper(serverDateFormat).fromTimestamp(lastTime));
                shouldFetchDeletedRecords = manifestProcessor.processBooleanExpression(configuration, expression, variables);
            }
        }
        catch (ManifestProcessingException e) {
            throw new SimpleSyncException(e);
        }

        return shouldFetchDeletedRecords;
    }
}
