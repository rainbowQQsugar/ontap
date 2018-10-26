package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/17/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// @Generated("org.jsonschema2pojo")
public class ObjectLayouts {

    @SerializedName("layouts")
    @Expose
    private Object layouts;
    @SerializedName("recordTypeMappings")
    @Expose
    private List<RecordTypeMapping> recordTypeMappings = new ArrayList<RecordTypeMapping>();
    @SerializedName("recordTypeSelectorRequired")
    @Expose
    private List<Boolean> recordTypeSelectorRequired = new ArrayList<Boolean>();

    /**
     *
     * @return
     * The layouts
     */
    public Object getLayouts() {
        return layouts;
    }

    /**
     *
     * @param layouts
     * The layouts
     */
    public void setLayouts(Object layouts) {
        this.layouts = layouts;
    }

    /**
     *
     * @return
     * The recordTypeMappings
     */
    public List<RecordTypeMapping> getRecordTypeMappings() {
        return recordTypeMappings;
    }

    /**
     *
     * @param recordTypeMappings
     * The recordTypeMappings
     */
    public void setRecordTypeMappings(List<RecordTypeMapping> recordTypeMappings) {
        this.recordTypeMappings = recordTypeMappings;
    }

    /**
     *
     * @return
     * The recordTypeSelectorRequired
     */
    public List<Boolean> getRecordTypeSelectorRequired() {
        return recordTypeSelectorRequired;
    }

    /**
     *
     * @param recordTypeSelectorRequired
     * The recordTypeSelectorRequired
     */
    public void setRecordTypeSelectorRequired(List<Boolean> recordTypeSelectorRequired) {
        this.recordTypeSelectorRequired = recordTypeSelectorRequired;
    }

}
