package com.salesforce.androidsyncengine.data.model;

/**
 * Created by bduggirala on 11/16/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// @Generated("org.jsonschema2pojo")
public class ChildRelationship {

    @SerializedName("cascadeDelete")
    @Expose
    private Boolean cascadeDelete;
    @SerializedName("childSObject")
    @Expose
    private String childSObject;
    @SerializedName("deprecatedAndHidden")
    @Expose
    private Boolean deprecatedAndHidden;
    @SerializedName("field")
    @Expose
    private String field;
    @SerializedName("junctionIdListName")
    @Expose
    private Object junctionIdListName;
    @SerializedName("junctionReferenceTo")
    @Expose
    private List<Object> junctionReferenceTo = new ArrayList<Object>();
    @SerializedName("relationshipName")
    @Expose
    private String relationshipName;
    @SerializedName("restrictedDelete")
    @Expose
    private Boolean restrictedDelete;

    /**
     *
     * @return
     * The cascadeDelete
     */
    public Boolean getCascadeDelete() {
        return cascadeDelete;
    }

    /**
     *
     * @param cascadeDelete
     * The cascadeDelete
     */
    public void setCascadeDelete(Boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }

    /**
     *
     * @return
     * The childSObject
     */
    public String getChildSObject() {
        return childSObject;
    }

    /**
     *
     * @param childSObject
     * The childSObject
     */
    public void setChildSObject(String childSObject) {
        this.childSObject = childSObject;
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
     * The field
     */
    public String getField() {
        return field;
    }

    /**
     *
     * @param field
     * The field
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     *
     * @return
     * The junctionIdListName
     */
    public Object getJunctionIdListName() {
        return junctionIdListName;
    }

    /**
     *
     * @param junctionIdListName
     * The junctionIdListName
     */
    public void setJunctionIdListName(Object junctionIdListName) {
        this.junctionIdListName = junctionIdListName;
    }

    /**
     *
     * @return
     * The junctionReferenceTo
     */
    public List<Object> getJunctionReferenceTo() {
        return junctionReferenceTo;
    }

    /**
     *
     * @param junctionReferenceTo
     * The junctionReferenceTo
     */
    public void setJunctionReferenceTo(List<Object> junctionReferenceTo) {
        this.junctionReferenceTo = junctionReferenceTo;
    }

    /**
     *
     * @return
     * The relationshipName
     */
    public String getRelationshipName() {
        return relationshipName;
    }

    /**
     *
     * @param relationshipName
     * The relationshipName
     */
    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    /**
     *
     * @return
     * The restrictedDelete
     */
    public Boolean getRestrictedDelete() {
        return restrictedDelete;
    }

    /**
     *
     * @param restrictedDelete
     * The restrictedDelete
     */
    public void setRestrictedDelete(Boolean restrictedDelete) {
        this.restrictedDelete = restrictedDelete;
    }

}

