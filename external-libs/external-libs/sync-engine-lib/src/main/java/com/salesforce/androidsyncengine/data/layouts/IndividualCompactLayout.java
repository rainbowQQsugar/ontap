package com.salesforce.androidsyncengine.data.layouts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by mewa on 7/12/17.
 */
// @Generated("org.jsonschema2pojo")
public class IndividualCompactLayout {

    @SerializedName("actions")
    @Expose
    private Object actions;
    @SerializedName("fieldItems")
    @Expose
    private List<LayoutItem> fieldItems = null;
    @SerializedName("id")
    @Expose
    private Object id;
    @SerializedName("imageItems")
    @Expose
    private Object imageItems;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("objectType")
    @Expose
    private String objectType;

    public Object getActions() {
        return actions;
    }

    public void setActions(Object actions) {
        this.actions = actions;
    }

    public List<LayoutItem> getFieldItems() {
        return fieldItems;
    }

    public void setFieldItems(List<LayoutItem> fieldItems) {
        this.fieldItems = fieldItems;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getImageItems() {
        return imageItems;
    }

    public void setImageItems(Object imageItems) {
        this.imageItems = imageItems;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

}
