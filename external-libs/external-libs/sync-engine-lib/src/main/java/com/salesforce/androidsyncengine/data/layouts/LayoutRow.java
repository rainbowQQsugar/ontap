package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
public class LayoutRow {

    @SerializedName("layoutItems")
    @Expose
    private List<LayoutItem> layoutItems = new ArrayList<LayoutItem>();
    @SerializedName("numItems")
    @Expose
    private Integer numItems;

    /**
     * @return The layoutItems
     */
    public List<LayoutItem> getLayoutItems() {
        return layoutItems;
    }

    /**
     * @param layoutItems The layoutItems
     */
    public void setLayoutItems(List<LayoutItem> layoutItems) {
        this.layoutItems = layoutItems;
    }

    /**
     * @return The numItems
     */
    public Integer getNumItems() {
        return numItems;
    }

    /**
     * @param numItems The numItems
     */
    public void setNumItems(Integer numItems) {
        this.numItems = numItems;
    }
}

