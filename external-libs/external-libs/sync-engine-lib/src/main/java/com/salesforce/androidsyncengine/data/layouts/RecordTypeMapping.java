package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/17/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
public class RecordTypeMapping {

    @SerializedName("available")
    @Expose
    private Boolean available;
    @SerializedName("defaultRecordTypeMapping")
    @Expose
    private Boolean defaultRecordTypeMapping;
    @SerializedName("layoutId")
    @Expose
    private String layoutId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("picklistsForRecordType")
    @Expose
    private List<Object> picklistsForRecordType = new ArrayList<Object>();
    @SerializedName("recordTypeId")
    @Expose
    private String recordTypeId;
    @SerializedName("urls")
    @Expose
    private LayoutUrls urls;

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
     * The layoutId
     */
    public String getLayoutId() {
        return layoutId;
    }

    /**
     *
     * @param layoutId
     * The layoutId
     */
    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
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
     * The picklistsForRecordType
     */
    public List<Object> getPicklistsForRecordType() {
        return picklistsForRecordType;
    }

    /**
     *
     * @param picklistsForRecordType
     * The picklistsForRecordType
     */
    public void setPicklistsForRecordType(List<Object> picklistsForRecordType) {
        this.picklistsForRecordType = picklistsForRecordType;
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
    public LayoutUrls getUrls() {
        return urls;
    }

    /**
     *
     * @param urls
     * The urls
     */
    public void setUrls(LayoutUrls urls) {
        this.urls = urls;
    }

}
