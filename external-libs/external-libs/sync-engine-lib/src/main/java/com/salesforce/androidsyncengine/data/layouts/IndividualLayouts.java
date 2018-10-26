package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
public class IndividualLayouts {

    @SerializedName("detailLayoutSections")
    @Expose
    private List<DetailLayoutSection> detailLayoutSections = new ArrayList<DetailLayoutSection>();

    @SerializedName("editLayoutSections")
    @Expose
    private List<EditLayoutSection> editLayoutSections = new ArrayList<EditLayoutSection>();

    /**
     *
     * @return
     * The detailLayoutSections
     */
    public List<DetailLayoutSection> getDetailLayoutSections() {
        return detailLayoutSections;
    }

    /**
     *
     * @param detailLayoutSections
     * The detailLayoutSections
     */
    public void setDetailLayoutSections(List<DetailLayoutSection> detailLayoutSections) {
        this.detailLayoutSections = detailLayoutSections;
    }

    /**
     *
     * @return
     * The detailLayoutSections
     */
    public List<EditLayoutSection> getEditLayoutSections() {
        return editLayoutSections;
    }

    /**
     *
     * @param editLayoutSections
     * The detailLayoutSections
     */
    public void setEditLayoutSections(List<EditLayoutSection> editLayoutSections) {
        this.editLayoutSections = editLayoutSections;
    }

}
