package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// @Generated("org.jsonschema2pojo")
public class LayoutItem {

    @SerializedName("editableForNew")
    @Expose
    private Boolean editableForNew;
    @SerializedName("editableForUpdate")
    @Expose
    private Boolean editableForUpdate;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("layoutComponents")
    @Expose
    private List<LayoutComponent> layoutComponents = new ArrayList<LayoutComponent>();
    @SerializedName("placeholder")
    @Expose
    private Boolean placeholder;
    @SerializedName("required")
    @Expose
    private Boolean required;

    /**
     *
     * @return
     * The editableForNew
     */
    public Boolean getEditableForNew() {
        return editableForNew;
    }

    /**
     *
     * @param editableForNew
     * The editableForNew
     */
    public void setEditableForNew(Boolean editableForNew) {
        this.editableForNew = editableForNew;
    }

    /**
     *
     * @return
     * The editableForUpdate
     */
    public Boolean getEditableForUpdate() {
        return editableForUpdate;
    }

    /**
     *
     * @param editableForUpdate
     * The editableForUpdate
     */
    public void setEditableForUpdate(Boolean editableForUpdate) {
        this.editableForUpdate = editableForUpdate;
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
     * The layoutComponents
     */
    public List<LayoutComponent> getLayoutComponents() {
        return layoutComponents;
    }

    /**
     *
     * @param layoutComponents
     * The layoutComponents
     */
    public void setLayoutComponents(List<LayoutComponent> layoutComponents) {
        this.layoutComponents = layoutComponents;
    }

    /**
     *
     * @return
     * The placeholder
     */
    public Boolean getPlaceholder() {
        return placeholder;
    }

    /**
     *
     * @param placeholder
     * The placeholder
     */
    public void setPlaceholder(Boolean placeholder) {
        this.placeholder = placeholder;
    }

    /**
     *
     * @return
     * The required
     */
    public Boolean getRequired() {
        return required;
    }

    /**
     *
     * @param required
     * The required
     */
    public void setRequired(Boolean required) {
        this.required = required;
    }

}
