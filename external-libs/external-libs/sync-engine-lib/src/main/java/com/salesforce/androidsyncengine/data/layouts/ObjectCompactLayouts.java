package com.salesforce.androidsyncengine.data.layouts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.salesforce.androidsyncengine.data.model.Urls_;

import java.util.List;

/**
 * Created by mewa on 7/11/17.
 */

// @Generated("org.jsonschema2pojo")
public class ObjectCompactLayouts {

    @SerializedName("compactLayouts")
    @Expose
    private Object compactLayouts;
    @SerializedName("defaultCompactLayoutId")
    @Expose
    private Object defaultCompactLayoutId;
    @SerializedName("recordTypeCompactLayoutMappings")
    @Expose
    private List<CompactLayoutRecordTypeMapping> recordTypeCompactLayoutMappings = null;
    @SerializedName("urls")
    @Expose
    private Urls_ urls;

    public Object getCompactLayouts() {
        return compactLayouts;
    }

    public void setCompactLayouts(Object compactLayouts) {
        this.compactLayouts = compactLayouts;
    }

    public Object getDefaultCompactLayoutId() {
        return defaultCompactLayoutId;
    }

    public void setDefaultCompactLayoutId(Object defaultCompactLayoutId) {
        this.defaultCompactLayoutId = defaultCompactLayoutId;
    }

    public List<CompactLayoutRecordTypeMapping> getRecordTypeCompactLayoutMappings() {
        return recordTypeCompactLayoutMappings;
    }

    public void setRecordTypeCompactLayoutMappings(List<CompactLayoutRecordTypeMapping> recordTypeCompactLayoutMappings) {
        this.recordTypeCompactLayoutMappings = recordTypeCompactLayoutMappings;
    }

    public Urls_ getUrls() {
        return urls;
    }

    public void setUrls(Urls_ urls) {
        this.urls = urls;
    }

}
