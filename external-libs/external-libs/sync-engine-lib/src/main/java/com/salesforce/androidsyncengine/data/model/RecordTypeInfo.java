package com.salesforce.androidsyncengine.data.model;

/**
 * Created by bduggirala on 11/16/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// @Generated("org.jsonschema2pojo")
public class RecordTypeInfo {

    @SerializedName("available")
    @Expose
    private Boolean available;
    @SerializedName("defaultRecordTypeMapping")
    @Expose
    private Boolean defaultRecordTypeMapping;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("recordTypeId")
    @Expose
    private String recordTypeId;
    @SerializedName("urls")
    @Expose
    private Urls urls;

    /**
     *
     * @return
     * The available
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     *
     * @param available
     * The available
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    /**
     *
     * @return
     * The defaultRecordTypeMapping
     */
    public Boolean getDefaultRecordTypeMapping() {
        return defaultRecordTypeMapping;
    }

    /**
     *
     * @param defaultRecordTypeMapping
     * The defaultRecordTypeMapping
     */
    public void setDefaultRecordTypeMapping(Boolean defaultRecordTypeMapping) {
        this.defaultRecordTypeMapping = defaultRecordTypeMapping;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The recordTypeId
     */
    public String getRecordTypeId() {
        return recordTypeId;
    }

    /**
     *
     * @param recordTypeId
     * The recordTypeId
     */
    public void setRecordTypeId(String recordTypeId) {
        this.recordTypeId = recordTypeId;
    }

    /**
     *
     * @return
     * The urls
     */
    public Urls getUrls() {
        return urls;
    }

    /**
     *
     * @param urls
     * The urls
     */
    public void setUrls(Urls urls) {
        this.urls = urls;
    }

}
