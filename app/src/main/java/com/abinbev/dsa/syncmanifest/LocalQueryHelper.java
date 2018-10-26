package com.abinbev.dsa.syncmanifest;

import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.LocalQuery;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jakub Stefanowski on 29.03.2017.
 */

public class LocalQueryHelper {

    private static final String TAG = "LocalQueryHelper";

    private static final int MAX_PAGE_SIZE = 1000;

    private Configuration configuration;

    private JexlManifestProcessor manifestProcessor;

    public LocalQueryHelper(Configuration configuration, JexlManifestProcessor manifestProcessor) {
        this.configuration = configuration;
        this.manifestProcessor = manifestProcessor;
    }

    public List<Object> execute(String queryName, String... args) throws ManifestProcessingException {
        LocalQuery localQuery = configuration.getLocalQuery(queryName);

        if (localQuery == null || TextUtils.isEmpty(localQuery.getQuery())) {
            throw new IllegalArgumentException("There is no local query with name: " + queryName);
        }

        try {
            String processedQuery = getQuery(queryName, args);
            return readFromQuery(processedQuery, localQuery.isExcludeClientIds());
        }
        catch (JSONException e) {
            Log.w(TAG, "Error while executing query: " + queryName, e);
        }

        return null;
    }

    public String getQuery(String queryName, String... args) throws ManifestProcessingException {
        LocalQuery localQuery = configuration.getLocalQuery(queryName);

        if (localQuery == null || TextUtils.isEmpty(localQuery.getQuery())) {
            throw new IllegalArgumentException("There is no local query with name: " + queryName);
        }

        List<String> requiredArguments = localQuery.getArgs();
        checkArguments(queryName, requiredArguments, args);

        // Create collection to map argument names with their values.
        Map<String, Object> argumentsMap = new HashMap<>();
        if (requiredArguments != null && !requiredArguments.isEmpty()) {
            for (int i = 0; i < requiredArguments.size(); i++) {
                argumentsMap.put(requiredArguments.get(i), args[i]);
            }
        }

        return manifestProcessor.processLocalQuery(configuration, localQuery.getQuery(), argumentsMap);
    }

    /** Execute query and convert result to string. */
    private List<Object> readFromQuery(String query, boolean excludeClientIds) throws JSONException {
//        Log.i(TAG, "query: " + query);
//        long start = SystemClock.elapsedRealtime();
        List<Object> result = new ArrayList<>();
        int pageIndex = 0;

        SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
        JSONArray recordsArray = dataManager.fetchSmartSQLQuery(query, pageIndex, MAX_PAGE_SIZE);

        while (recordsArray != null && recordsArray.length() > 0) {
            for (int i = 0; i < recordsArray.length(); i++) {
                Object value = recordsArray.getJSONArray(i).opt(0);

                // Filter out client ids if required.
                if (!excludeClientIds || !dataManager.isClientId((String) value)) {
                    result.add(value);
                }
            }

            pageIndex++;
            recordsArray = dataManager.fetchSmartSQLQuery(query, pageIndex, MAX_PAGE_SIZE);
        }

//        long end = SystemClock.elapsedRealtime();
//        Log.i(TAG, "result: " + result);
//        Log.i(TAG, "in time: " + (end - start) + "ms");
        return result.isEmpty() ? null : result;
    }

    private void checkArguments(String queryName, List<String> requiredArguments, String[] receivedArguments) {
        int requiredArgumentsCount = sizeOf(requiredArguments);
        int currentArgumentsCount = sizeOf(receivedArguments);

        if (requiredArgumentsCount != currentArgumentsCount) {
            throw new IllegalStateException("Local query: " + queryName + " requires "
                    + requiredArgumentsCount + " arguments but has received " + currentArgumentsCount);
        }
    }

    private static int sizeOf(Collection c) {
        return c == null ? 0 : c.size();
    }

    private static int sizeOf(Object[] arr) {
        return arr == null ? 0 : arr.length;
    }
}
