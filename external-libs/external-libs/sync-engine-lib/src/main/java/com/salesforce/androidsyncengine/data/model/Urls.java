package com.salesforce.androidsyncengine.data.model;

/**
 * Created by bduggirala on 11/16/15.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
public class Urls {

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
