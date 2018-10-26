package com.abinbev.dsa.syncmanifest;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.SFQueryFilter;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.jexl3.MapContext;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Manifest Processor which uses Java Expression language to parse queries.
 *
 * Created by Jakub Stefanowski on 27.03.2017.
 */

public class JexlManifestProcessor extends ManifestProcessor {

    private static final String TAG = JexlManifestProcessor.class.getSimpleName();

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    private static final String VARIABLE_DATE = "date";
    private static final String VARIABLE_DATE_TIME = "dateTime";
    private static final String VARIABLE_CURRENT_USER = "currentUser";
    private static final String VARIABLE_APP_PREFERENCE_UTILS = "appPreferenceUtils";
    private static final String VARIABLE_LOCAL_QUERIES = "localQueries";
    private static final String VARIABLE_ARGS = "args";
    private static final String VARIABLE_PARAMS = "params";

    private static final JexlInfo QUERY_INFO = new JexlInfo(TAG, 0, 0);

    private static final int MAX_ITEMS_PER_QUERY = 300;

    static {
        SIMPLE_DATE_FORMAT.setTimeZone(TIME_ZONE_UTC);
        SIMPLE_DATE_TIME_FORMAT.setTimeZone(TIME_ZONE_UTC);
    }

    private final JexlEngine jexlEngine;

    private final AppPreferenceUtils appPreferenceUtils;

    private final QueryProcessor queryProcessor;

    public JexlManifestProcessor() {
        jexlEngine = new JexlBuilder().create();
        appPreferenceUtils = AppPreferenceUtils.getInstance(ABInBevApp.getAppContext());
        queryProcessor = new QueryProcessor(jexlEngine);
    }

    @Override
    public String processLocalQuery(Configuration configuration, SFObjectMetadata objectMetadata, String query) throws ManifestProcessingException {
        if (TextUtils.isEmpty(query)) return query;

        try {
            LocalQueryHelper queryHelper = new LocalQueryHelper(configuration, this);
            JexlContext jexlContext = createBaseContext();
            jexlContext.set(VARIABLE_LOCAL_QUERIES, queryHelper);

            return queryProcessor.processQuery(query, jexlContext, QUERY_INFO);
        }
        catch (Exception e) {
            Log.w(TAG, "Error while processing local query on object: " + objectMetadata.getNameWitoutNameSpace() + " query: " + query);
            throw createException(e, "Error while processing local query on object: " + objectMetadata.getNameWitoutNameSpace());
        }
    }

    @Override
    public List<String> processFilter(Configuration configuration, SFObjectMetadata objectMetadata,
                                      SFQueryFilter queryFilter) throws ManifestProcessingException {
        return processFilter(configuration, objectMetadata, queryFilter, null);
    }

    @Override
    public List<String> processFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter,
                                      Map<String, Object> additionalVariables) throws ManifestProcessingException {
        if (queryFilter == null || TextUtils.isEmpty(queryFilter.getQuery())) return Collections.emptyList();

        try {
            LocalQueryHelper queryHelper = new LocalQueryHelper(configuration, this);
            JexlContext jexlContext = createBaseContext();
            jexlContext.set(VARIABLE_LOCAL_QUERIES, queryHelper);
            addAll(jexlContext, additionalVariables);

            if (isActive(queryFilter,  jexlContext)) {
                return queryProcessor.processQuery(queryFilter.getQuery(), jexlContext, QUERY_INFO, MAX_ITEMS_PER_QUERY);
            }
            else {
                Log.d(TAG, "Inactive query: " + queryFilter.getQuery() + " on object: " + objectMetadata.getNameWitoutNameSpace());
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Error while processing filter query on object: " + objectMetadata.getNameWitoutNameSpace() + " query: " + queryFilter.getQuery());
            throw createException(e, "Error while processing filter query on object: " + objectMetadata.getNameWitoutNameSpace());
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> processDynamicFetchFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter, Map<String, String> params) throws ManifestProcessingException {
        if (queryFilter == null || TextUtils.isEmpty(queryFilter.getQuery())) return Collections.emptyList();

        try {
            LocalQueryHelper queryHelper = new LocalQueryHelper(configuration, this);
            DynamicFetchParamsHelper paramsHelper = new DynamicFetchParamsHelper(objectMetadata.getNameWitoutNameSpace(), params);
            JexlContext jexlContext = createBaseContext();
            jexlContext.set(VARIABLE_LOCAL_QUERIES, queryHelper);
            jexlContext.set(VARIABLE_PARAMS, paramsHelper);

            if (isActive(queryFilter,  jexlContext)) {
                return queryProcessor.processQuery(queryFilter.getQuery(), jexlContext, QUERY_INFO, MAX_ITEMS_PER_QUERY);
            }
            else {
                Log.d(TAG, "Inactive query: " + queryFilter.getQuery() + " on object: " + objectMetadata.getNameWitoutNameSpace());
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Error while processing dynamic fetch filter on object: " + objectMetadata.getNameWitoutNameSpace() + " query: " + queryFilter.getQuery());
            throw createException(e, "Error while processing dynamic fetch filter on object: " + objectMetadata.getNameWitoutNameSpace());
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> processCheckLastModifyFilter(Configuration configuration, SFObjectMetadata objectMetadata, String queryFilter) throws ManifestProcessingException {
        if (queryFilter == null || TextUtils.isEmpty(queryFilter)) return Collections.emptyList();

        try {
            JexlContext jexlContext = createBaseContext();
            return queryProcessor.processQuery(queryFilter, jexlContext, QUERY_INFO, MAX_ITEMS_PER_QUERY);
        }
        catch (Exception e) {
            Log.w(TAG, "Error while processing checkLastModifyFilter query on object: " + objectMetadata.getNameWitoutNameSpace() + " query: " + queryFilter);
            throw createException(e, "Error while processing checkLastModifyFilter query on object: " + objectMetadata.getNameWitoutNameSpace());
        }
    }

    @Override
    public boolean processBooleanExpression(Configuration configuration, String expression, Map<String, Object> additionalVariables) throws ManifestProcessingException {
        if (TextUtils.isEmpty(expression)) return false;

        try {
            JexlContext jexlContext = createBaseContext();
            addAll(jexlContext, additionalVariables);

            Object result = queryProcessor.processExpression(expression, jexlContext, QUERY_INFO);
            if (result instanceof Boolean) {
                return (boolean) result;
            } else {
                return Boolean.parseBoolean(String.valueOf(result));
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Error while processing boolean expression: " + expression);
            throw createException(e, "Error while processing boolean expression: " + expression);
        }
    }

    /** Allows to process local SmartSql queries. */
    public String processLocalQuery(Configuration configuration, String query, Map<String, Object> argumentsMap) throws ManifestProcessingException {
        if (TextUtils.isEmpty(query)) return query;

        try {
            LocalQueryHelper queryHelper = new LocalQueryHelper(configuration, this);
            JexlContext jexlContext = createBaseContext();
            jexlContext.set(VARIABLE_LOCAL_QUERIES, queryHelper);
            jexlContext.set(VARIABLE_ARGS, argumentsMap);

            return queryProcessor.processQuery(query, jexlContext, QUERY_INFO);
        }
        catch (Exception e) {
            Log.w(TAG, "Couldn't process local query: " + query);
            throw createException(e, "Couldn't process local query");
        }
    }

    public LocalQueryHelper getLocalQueryHelper() {
        Configuration configuration = PreferenceUtils.getConfiguration(ABInBevApp.getAppContext());
        return new LocalQueryHelper(configuration, this);
    }

    /** Evaluates if query filter is active. */
    private boolean isActive(SFQueryFilter filter, JexlContext jexlContext) {
        // By default all filters are active.
        if (TextUtils.isEmpty(filter.getIsActive())) {
            return true;
        }

        Object result = queryProcessor.processExpression(filter.getIsActive(), jexlContext, QUERY_INFO);
        if (result instanceof Boolean) {
            return (boolean) result;
        }
        else {
            return Boolean.parseBoolean(String.valueOf(result));
        }
    }

    private ManifestProcessingException createException(Exception e, String message) {
        Throwable cause = e.getCause();
        String exceptionMessage = cause == null ? e.getMessage() : cause.getMessage();
        String errorMessage = message + ". " + exceptionMessage;
        return new ManifestProcessingException(errorMessage, e);
    }

    private JexlContext createBaseContext() {
        DateHelper dateHelper = new DateHelper(SIMPLE_DATE_FORMAT, TIME_ZONE_UTC);
        DateHelper dateTimeHelper = new DateHelper(SIMPLE_DATE_TIME_FORMAT);

        JexlContext jexlContext = new MapContext();
        jexlContext.set(VARIABLE_DATE, dateHelper);
        jexlContext.set(VARIABLE_DATE_TIME, dateTimeHelper);
        jexlContext.set(VARIABLE_CURRENT_USER, getCurrentUser());
        jexlContext.set(VARIABLE_APP_PREFERENCE_UTILS, appPreferenceUtils);

        return jexlContext;
    }

    private static void addAll(JexlContext context, Map<String, Object> params) {
        if (params == null || params.isEmpty()) return;

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
    }

    private User currentUser;

    private User getCurrentUser() {
        if (currentUser == null) {
            String userId = UserAccountManager.getInstance().getStoredUserId();
            currentUser = User.getUserByUserId(userId);
        }
        else {
            String userId = UserAccountManager.getInstance().getStoredUserId();
            if (!Objects.equals(userId, currentUser.getId())) {
                currentUser = User.getUserByUserId(userId);
            }
        }

        return currentUser;
    }
}
