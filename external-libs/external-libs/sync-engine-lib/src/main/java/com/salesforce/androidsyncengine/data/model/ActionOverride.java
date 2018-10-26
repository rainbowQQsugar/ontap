package com.salesforce.androidsyncengine.data.model;

/**
 * Created by bduggirala on 11/16/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
public class ActionOverride {

    @SerializedName("isAvailableInTouch")
    @Expose
    private Boolean isAvailableInTouch;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("pageId")
    @Expose
    private String pageId;
    @SerializedName("url")
    @Expose
    private String url;

    /**
     * @return The isAvailableInTouch
     */
    public Boolean getIsAvailableInTouch() {
        return isAvailableInTouch;
    }

    /**
     * @param isAvailableInTouch The isAvailableInTouch
     */
    public void setIsAvailableInTouch(Boolean isAvailableInTouch) {
        this.isAvailableInTouch = isAvailableInTouch;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The pageId
     */
    public String getPageId() {
        return pageId;
    }

    /**
     * @param pageId The pageId
     */
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }
}

