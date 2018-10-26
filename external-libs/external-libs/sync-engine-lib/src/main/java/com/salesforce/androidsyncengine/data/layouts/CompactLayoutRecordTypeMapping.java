package com.salesforce.androidsyncengine.data.layouts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.salesforce.androidsyncengine.data.model.Urls_;

/**
 * Created by mewa on 7/12/17.
 */

public class CompactLayoutRecordTypeMapping {

    @SerializedName("available")
    @Expose
    private Boolean available;
    @SerializedName("compactLayoutId")
    @Expose
    private Object compactLayoutId;
    @SerializedName("compactLayoutName")
    @Expose
    private String compactLayoutName;
    @SerializedName("recordTypeId")
    @Expose
    private String recordTypeId;
    @SerializedName("recordTypeName")
    @Expose
    private String recordTypeName;
    @SerializedName("urls")
    @Expose
    private Urls_ urls;

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Object getCompactLayoutId() {
        return compactLayoutId;
    }

    public void setCompactLayoutId(Object compactLayoutId) {
        this.compactLayoutId = compactLayoutId;
    }

    public String getCompactLayoutName() {
        return compactLayoutName;
    }

    public void setCompactLayoutName(String compactLayoutName) {
        this.compactLayoutName = compactLayoutName;
    }

    public String getRecordTypeId() {
        return recordTypeId;
    }

    public void setRecordTypeId(String recordTypeId) {
        this.recordTypeId = recordTypeId;
    }

    public String getRecordTypeName() {
        return recordTypeName;
    }

    public void setRecordTypeName(String recordTypeName) {
        this.recordTypeName = recordTypeName;
    }

    public Urls_ getUrls() {
        return urls;
    }

    public void setUrls(Urls_ urls) {
        this.urls = urls;
    }

}
