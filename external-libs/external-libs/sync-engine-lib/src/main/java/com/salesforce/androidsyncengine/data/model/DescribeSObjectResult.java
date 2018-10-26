package com.salesforce.androidsyncengine.data.model;

/**
 * Created by bduggirala on 11/16/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// @Generated("org.jsonschema2pojo")
public class DescribeSObjectResult {

    @SerializedName("actionOverrides")
    @Expose
    private List<ActionOverride> actionOverrides = new ArrayList<ActionOverride>();
    @SerializedName("activateable")
    @Expose
    private Boolean activateable;
    @SerializedName("childRelationships")
    @Expose
    private List<ChildRelationship> childRelationships = new ArrayList<ChildRelationship>();
    @SerializedName("compactLayoutable")
    @Expose
    private Boolean compactLayoutable;
    @SerializedName("createable")
    @Expose
    private Boolean createable;
    @SerializedName("custom")
    @Expose
    private Boolean custom;
    @SerializedName("customSetting")
    @Expose
    private Boolean customSetting;
    @SerializedName("deletable")
    @Expose
    private Boolean deletable;
    @SerializedName("deprecatedAndHidden")
    @Expose
    private Boolean deprecatedAndHidden;
    @SerializedName("feedEnabled")
    @Expose
    private Boolean feedEnabled;
    @SerializedName("fields")
    @Expose
    private List<Field> fields = new ArrayList<Field>();
    @SerializedName("keyPrefix")
    @Expose
    private String keyPrefix;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("labelPlural")
    @Expose
    private String labelPlural;
    @SerializedName("layoutable")
    @Expose
    private Boolean layoutable;
    @SerializedName("listviewable")
    @Expose
    private Object listviewable;
    @SerializedName("lookupLayoutable")
    @Expose
    private Object lookupLayoutable;
    @SerializedName("mergeable")
    @Expose
    private Boolean mergeable;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("namedLayoutInfos")
    @Expose
    private List<Object> namedLayoutInfos = new ArrayList<Object>();
    @SerializedName("queryable")
    @Expose
    private Boolean queryable;
    @SerializedName("recordTypeInfos")
    @Expose
    private List<RecordTypeInfo> recordTypeInfos = new ArrayList<RecordTypeInfo>();
    @SerializedName("replicateable")
    @Expose
    private Boolean replicateable;
    @SerializedName("retrieveable")
    @Expose
    private Boolean retrieveable;
    @SerializedName("searchLayoutable")
    @Expose
    private Boolean searchLayoutable;
    @SerializedName("searchable")
    @Expose
    private Boolean searchable;
    @SerializedName("triggerable")
    @Expose
    private Boolean triggerable;
    @SerializedName("undeletable")
    @Expose
    private Boolean undeletable;
    @SerializedName("updateable")
    @Expose
    private Boolean updateable;
    @SerializedName("urls")
    @Expose
    private Urls_ urls;

    /**
     *
     * @return
     * The actionOverrides
     */
    public List<ActionOverride> getActionOverrides() {
        return actionOverrides;
    }

    /**
     *
     * @param actionOverrides
     * The actionOverrides
     */
    public void setActionOverrides(List<ActionOverride> actionOverrides) {
        this.actionOverrides = actionOverrides;
    }

    /**
     *
     * @return
     * The activateable
     */
    public Boolean getActivateable() {
        return activateable;
    }

    /**
     *
     * @param activateable
     * The activateable
     */
    public void setActivateable(Boolean activateable) {
        this.activateable = activateable;
    }

    /**
     *
     * @return
     * The childRelationships
     */
    public List<ChildRelationship> getChildRelationships() {
        return childRelationships;
    }

    /**
     *
     * @param childRelationships
     * The childRelationships
     */
    public void setChildRelationships(List<ChildRelationship> childRelationships) {
        this.childRelationships = childRelationships;
    }

    /**
     *
     * @return
     * The compactLayoutable
     */
    public Boolean getCompactLayoutable() {
        return compactLayoutable;
    }

    /**
     *
     * @param compactLayoutable
     * The compactLayoutable
     */
    public void setCompactLayoutable(Boolean compactLayoutable) {
        this.compactLayoutable = compactLayoutable;
    }

    /**
     *
     * @return
     * The createable
     */
    public Boolean getCreateable() {
        return createable;
    }

    /**
     *
     * @param createable
     * The createable
     */
    public void setCreateable(Boolean createable) {
        this.createable = createable;
    }

    /**
     *
     * @return
     * The custom
     */
    public Boolean getCustom() {
        return custom;
    }

    /**
     *
     * @param custom
     * The custom
     */
    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    /**
     *
     * @return
     * The customSetting
     */
    public Boolean getCustomSetting() {
        return customSetting;
    }

    /**
     *
     * @param customSetting
     * The customSetting
     */
    public void setCustomSetting(Boolean customSetting) {
        this.customSetting = customSetting;
    }

    /**
     *
     * @return
     * The deletable
     */
    public Boolean getDeletable() {
        return deletable;
    }

    /**
     *
     * @param deletable
     * The deletable
     */
    public void setDeletable(Boolean deletable) {
        this.deletable = deletable;
    }

    /**
     *
     * @return
     * The deprecatedAndHidden
     */
    public Boolean getDeprecatedAndHidden() {
        return deprecatedAndHidden;
    }

    /**
     *
     * @param deprecatedAndHidden
     * The deprecatedAndHidden
     */
    public void setDeprecatedAndHidden(Boolean deprecatedAndHidden) {
        this.deprecatedAndHidden = deprecatedAndHidden;
    }

    /**
     *
     * @return
     * The feedEnabled
     */
    public Boolean getFeedEnabled() {
        return feedEnabled;
    }

    /**
     *
     * @param feedEnabled
     * The feedEnabled
     */
    public void setFeedEnabled(Boolean feedEnabled) {
        this.feedEnabled = feedEnabled;
    }

    /**
     *
     * @return
     * The fields
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     *
     * @param fields
     * The fields
     */
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    /**
     *
     * @return
     * The keyPrefix
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     *
     * @param keyPrefix
     * The keyPrefix
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /**
     *
     * @return
     * The label
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     * The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     * The labelPlural
     */
    public String getLabelPlural() {
        return labelPlural;
    }

    /**
     *
     * @param labelPlural
     * The labelPlural
     */
    public void setLabelPlural(String labelPlural) {
        this.labelPlural = labelPlural;
    }

    /**
     *
     * @return
     * The layoutable
     */
    public Boolean getLayoutable() {
        return layoutable;
    }

    /**
     *
     * @param layoutable
     * The layoutable
     */
    public void setLayoutable(Boolean layoutable) {
        this.layoutable = layoutable;
    }

    /**
     *
     * @return
     * The listviewable
     */
    public Object getListviewable() {
        return listviewable;
    }

    /**
     *
     * @param listviewable
     * The listviewable
     */
    public void setListviewable(Object listviewable) {
        this.listviewable = listviewable;
    }

    /**
     *
     * @return
     * The lookupLayoutable
     */
    public Object getLookupLayoutable() {
        return lookupLayoutable;
    }

    /**
     *
     * @param lookupLayoutable
     * The lookupLayoutable
     */
    public void setLookupLayoutable(Object lookupLayoutable) {
        this.lookupLayoutable = lookupLayoutable;
    }

    /**
     *
     * @return
     * The mergeable
     */
    public Boolean getMergeable() {
        return mergeable;
    }

    /**
     *
     * @param mergeable
     * The mergeable
     */
    public void setMergeable(Boolean mergeable) {
        this.mergeable = mergeable;
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
     * The namedLayoutInfos
     */
    public List<Object> getNamedLayoutInfos() {
        return namedLayoutInfos;
    }

    /**
     *
     * @param namedLayoutInfos
     * The namedLayoutInfos
     */
    public void setNamedLayoutInfos(List<Object> namedLayoutInfos) {
        this.namedLayoutInfos = namedLayoutInfos;
    }

    /**
     *
     * @return
     * The queryable
     */
    public Boolean getQueryable() {
        return queryable;
    }

    /**
     *
     * @param queryable
     * The queryable
     */
    public void setQueryable(Boolean queryable) {
        this.queryable = queryable;
    }

    /**
     *
     * @return
     * The recordTypeInfos
     */
    public List<RecordTypeInfo> getRecordTypeInfos() {
        return recordTypeInfos;
    }

    /**
     *
     * @param recordTypeInfos
     * The recordTypeInfos
     */
    public void setRecordTypeInfos(List<RecordTypeInfo> recordTypeInfos) {
        this.recordTypeInfos = recordTypeInfos;
    }

    /**
     *
     * @return
     * The replicateable
     */
    public Boolean getReplicateable() {
        return replicateable;
    }

    /**
     *
     * @param replicateable
     * The replicateable
     */
    public void setReplicateable(Boolean replicateable) {
        this.replicateable = replicateable;
    }

    /**
     *
     * @return
     * The retrieveable
     */
    public Boolean getRetrieveable() {
        return retrieveable;
    }

    /**
     *
     * @param retrieveable
     * The retrieveable
     */
    public void setRetrieveable(Boolean retrieveable) {
        this.retrieveable = retrieveable;
    }

    /**
     *
     * @return
     * The searchLayoutable
     */
    public Boolean getSearchLayoutable() {
        return searchLayoutable;
    }

    /**
     *
     * @param searchLayoutable
     * The searchLayoutable
     */
    public void setSearchLayoutable(Boolean searchLayoutable) {
        this.searchLayoutable = searchLayoutable;
    }

    /**
     *
     * @return
     * The searchable
     */
    public Boolean getSearchable() {
        return searchable;
    }

    /**
     *
     * @param searchable
     * The searchable
     */
    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    /**
     *
     * @return
     * The triggerable
     */
    public Boolean getTriggerable() {
        return triggerable;
    }

    /**
     *
     * @param triggerable
     * The triggerable
     */
    public void setTriggerable(Boolean triggerable) {
        this.triggerable = triggerable;
    }

    /**
     *
     * @return
     * The undeletable
     */
    public Boolean getUndeletable() {
        return undeletable;
    }

    /**
     *
     * @param undeletable
     * The undeletable
     */
    public void setUndeletable(Boolean undeletable) {
        this.undeletable = undeletable;
    }

    /**
     *
     * @return
     * The updateable
     */
    public Boolean getUpdateable() {
        return updateable;
    }

    /**
     *
     * @param updateable
     * The updateable
     */
    public void setUpdateable(Boolean updateable) {
        this.updateable = updateable;
    }

    /**
     *
     * @return
     * The urls
     */
    public Urls_ getUrls() {
        return urls;
    }

    /**
     *
     * @param urls
     * The urls
     */
    public void setUrls(Urls_ urls) {
        this.urls = urls;
    }

}

