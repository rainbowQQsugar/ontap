package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
public class EditLayoutSection implements LayoutSection {

    @SerializedName("columns")
    @Expose
    private Integer columns;
    @SerializedName("heading")
    @Expose
    private String heading;
    @SerializedName("layoutRows")
    @Expose
    private List<LayoutRow> layoutRows = new ArrayList<LayoutRow>();
    @SerializedName("rows")
    @Expose
    private Integer rows;
    @SerializedName("tabOrder")
    @Expose
    private String tabOrder;
    @SerializedName("useCollapsibleSection")
    @Expose
    private Boolean useCollapsibleSection;
    @SerializedName("useHeading")
    @Expose
    private Boolean useHeading;

    /**
     *
     * @return
     * The columns
     */
    public Integer getColumns() {
        return columns;
    }

    /**
     *
     * @param columns
     * The columns
     */
    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    /**
     *
     * @return
     * The heading
     */
    public String getHeading() {
        return heading;
    }

    /**
     *
     * @param heading
     * The heading
     */
    public void setHeading(String heading) {
        this.heading = heading;
    }

    /**
     *
     * @return
     * The layoutRows
     */
    public List<LayoutRow> getLayoutRows() {
        return layoutRows;
    }

    /**
     *
     * @param layoutRows
     * The layoutRows
     */
    public void setLayoutRows(List<LayoutRow> layoutRows) {
        this.layoutRows = layoutRows;
    }

    /**
     *
     * @return
     * The rows
     */
    public Integer getRows() {
        return rows;
    }

    /**
     *
     * @param rows
     * The rows
     */
    public void setRows(Integer rows) {
        this.rows = rows;
    }

    /**
     *
     * @return
     * The tabOrder
     */
    public String getTabOrder() {
        return tabOrder;
    }

    /**
     *
     * @param tabOrder
     * The tabOrder
     */
    public void setTabOrder(String tabOrder) {
        this.tabOrder = tabOrder;
    }

    /**
     *
     * @return
     * The useCollapsibleSection
     */
    public Boolean getUseCollapsibleSection() {
        return useCollapsibleSection;
    }

    /**
     *
     * @param useCollapsibleSection
     * The useCollapsibleSection
     */
    public void setUseCollapsibleSection(Boolean useCollapsibleSection) {
        this.useCollapsibleSection = useCollapsibleSection;
    }

    /**
     *
     * @return
     * The useHeading
     */
    public Boolean getUseHeading() {
        return useHeading;
    }

    /**
     *
     * @param useHeading
     * The useHeading
     */
    public void setUseHeading(Boolean useHeading) {
        this.useHeading = useHeading;
    }

}
