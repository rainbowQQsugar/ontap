package com.salesforce.androidsyncengine.syncsteps.localcleanup;

import android.content.Context;
import android.text.TextUtils;

import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.syncsteps.ConditionalSyncStep;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Class that checks if {@link LocalCleanupSyncStep} should be executed.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
class LocalCleanupCondition implements ConditionalSyncStep.Condition {

    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US); // Mon, 14 Apr 2014 21:40:15 GMT

    private final Context context;

    private final ManifestProcessor manifestProcessor;

    private final Configuration configuration;

    public LocalCleanupCondition(SyncControls syncControls) {
        this.context = syncControls.getContext();
        this.manifestProcessor = syncControls.getManifestProcessor();
        this.configuration = PreferenceUtils.getConfiguration(context);
    }

    @Override
    public boolean accept(SyncControls syncControls) throws SyncException {
        try {
            boolean shouldRunLocalCleanup;

            // Check if local cleanup should be executed.
            String expression = configuration.getShouldRunLocalCleanup();
            if (TextUtils.isEmpty(expression)) {
                shouldRunLocalCleanup = true;
            }
            else {
                HashMap<String, Object> variables = new HashMap<>();
                long lastTime = PreferenceUtils.getLastLocalCleanup(context);
                variables.put("lastCleanup", new DateHelper(serverDateFormat).fromTimestamp(lastTime));
                shouldRunLocalCleanup = manifestProcessor.processBooleanExpression(configuration, expression, variables);
            }

            return shouldRunLocalCleanup;
        } catch (ManifestProcessingException e) {
            throw new SimpleSyncException(e);
        }
    }
}
