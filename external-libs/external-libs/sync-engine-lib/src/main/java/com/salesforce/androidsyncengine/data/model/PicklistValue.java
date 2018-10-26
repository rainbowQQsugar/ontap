package com.salesforce.androidsyncengine.data.model;

/**
 * Created by bduggirala on 11/16/15.
 */
// import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// @Generated("org.jsonschema2pojo")
public class PicklistValue {

    @SerializedName("active")
    @Expose
    private Boolean active;
    @SerializedName("defaultValue")
    @Expose
    private Boolean defaultValue;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("validFor")
    @Expose
    private Object validFor;
    @SerializedName("value")
    @Expose
    private String value;

    /**
     *
     * @return
     * The active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     *
     * @param active
     * The active
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     *
     * @return
     * The defaultValue
     */
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    /**
     *
     * @param defaultValue
     * The defaultValue
     */
    public void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
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
     * The validFor
     */
    public Object getValidFor() {
        return validFor;
    }

    /**
     *
     * @param validFor
     * The validFor
     */
    public void setValidFor(Object validFor) {
        this.validFor = validFor;
    }

    /**
     *
     * @return
     * The value
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value
     * The value
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return label;
    }
}

