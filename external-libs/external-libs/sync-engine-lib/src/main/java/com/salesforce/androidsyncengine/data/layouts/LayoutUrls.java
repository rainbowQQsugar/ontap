package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/17/15.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// @Generated("org.jsonschema2pojo")
public class LayoutUrls {

    @SerializedName("layout")
    @Expose
    private String layout;

    /**
     *
     * @return
     * The layout
     */
    public String getLayout() {
        return layout;
    }

    /**
     *
     * @param layout
     * The layout
     */
    public void setLayout(String layout) {
        this.layout = layout;
    }

}
