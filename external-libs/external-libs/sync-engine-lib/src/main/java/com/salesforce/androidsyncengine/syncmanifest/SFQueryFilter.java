package com.salesforce.androidsyncengine.syncmanifest;

/**
 * Created by Jakub Stefanowski on 12.04.2017.
 */

public class SFQueryFilter {

    private String isActive;
    private String query;

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
