package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
public class LayoutComponent {

    @SerializedName("details")
    @Expose
    private Details details;
    @SerializedName("displayLines")
    @Expose
    private Integer displayLines;
    @SerializedName("tabOrder")
    @Expose
    private Integer tabOrder;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("value")
    @Expose
    private String value;

    /**
     *
     * @return
     * The details
     */
    public Details getDetails() {
        return details;
    }

    /**
     *
     * @param details
     * The details
     */
    public void setDetails(Details details) {
        this.details = details;
    }

    /**
     *
     * @return
     * The displayLines
     */
    public Integer getDisplayLines() {
        return displayLines;
    }

    /**
     *
     * @param displayLines
     * The displayLines
     */
    public void setDisplayLines(Integer displayLines) {
        this.displayLines = displayLines;
    }

    /**
     *
     * @return
     * The tabOrder
     */
    public Integer getTabOrder() {
        return tabOrder;
    }

    /**
     *
     * @param tabOrder
     * The tabOrder
     */
    public void setTabOrder(Integer tabOrder) {
        this.tabOrder = tabOrder;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
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

}
