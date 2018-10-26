package com.salesforce.androidsyncengine.data.layouts;

/**
 * Created by bduggirala on 11/18/15.
 */

import java.util.List;
public interface LayoutSection {

    Integer getColumns();

    String getHeading();

    List<LayoutRow> getLayoutRows();

    Integer getRows();

    String getTabOrder();

    Boolean getUseCollapsibleSection();

    Boolean getUseHeading();
}
