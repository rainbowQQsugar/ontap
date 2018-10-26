package com.salesforce.androidsyncengine.syncmanifest;

import java.util.List;

/**
 * Created by Jakub Stefanowski on 05.04.2017.
 */

public class DynamicFetchConfig {

    private static final long DEFAULT_FETCH_VALIDITY = 2L * 60L * 60L; // 2h

    private String name;

    private boolean fetchDeletedRecords;

    private List<SFQueryFilter> filters;

    private Long fetchValidity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFilters(List<SFQueryFilter> filters) {
        this.filters = filters;
    }

    public List<SFQueryFilter> getFilters() {
        return filters;
    }

    public boolean isFetchDeletedRecords() {
        return fetchDeletedRecords;
    }

    public void setFetchDeletedRecords(boolean fetchDeletedRecords) {
        this.fetchDeletedRecords = fetchDeletedRecords;
    }

    /** Get fetch validity (in seconds). */
    public long getFetchValidity() {
        return fetchValidity == null ? DEFAULT_FETCH_VALIDITY : fetchValidity;
    }

    /** Set fetch validity (in seconds). */
    public void setFetchValidity(Long fetchValidity) {
        this.fetchValidity = fetchValidity;
    }
}
