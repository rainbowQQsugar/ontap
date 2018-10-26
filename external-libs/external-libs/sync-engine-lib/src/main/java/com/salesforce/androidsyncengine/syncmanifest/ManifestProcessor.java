package com.salesforce.androidsyncengine.syncmanifest;

import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Jakub Stefanowski on 27.03.2017.
 */

public abstract class ManifestProcessor {

    public static final ManifestProcessor EMPTY = new ManifestProcessor() {
        @Override
        public boolean processBooleanExpression(Configuration configuration, String expression, Map<String, Object> additionalVariables) throws ManifestProcessingException {
            return false;
        }

        @Override
        public String processLocalQuery(Configuration configuration, SFObjectMetadata objectMetadata, String query) throws ManifestProcessingException {
            return query;
        }

        @Override
        public List<String> processFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter) throws ManifestProcessingException {
            return Collections.emptyList();
        }

        @Override
        public List<String> processFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter, Map<String, Object> additionalVariables) throws ManifestProcessingException {
            return Collections.emptyList();
        }

        @Override
        public List<String> processDynamicFetchFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter, Map<String, String> params) throws ManifestProcessingException {
            return Collections.emptyList();
        }

        @Override
        public List<String> processCheckLastModifyFilter(Configuration configuration, SFObjectMetadata objectMetadata, String queryFilter) throws ManifestProcessingException {
            return Collections.emptyList();
        }
    };

    public abstract boolean processBooleanExpression(Configuration configuration, String expression, Map<String, Object> additionalVariables) throws ManifestProcessingException;

    public abstract String processLocalQuery(Configuration configuration, SFObjectMetadata objectMetadata, String query) throws ManifestProcessingException;

    /** Processes query from parameter. Query can be splitted to multiple smaller queries if it will be too big. */
    public abstract List<String> processFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter) throws ManifestProcessingException;

    public abstract List<String> processFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter, Map<String, Object> additionalVariables) throws ManifestProcessingException;

    public abstract List<String> processDynamicFetchFilter(Configuration configuration, SFObjectMetadata objectMetadata, SFQueryFilter queryFilter, Map<String, String> params) throws ManifestProcessingException;

    public abstract List<String> processCheckLastModifyFilter(Configuration configuration, SFObjectMetadata objectMetadata, String queryFilter) throws ManifestProcessingException;
}
