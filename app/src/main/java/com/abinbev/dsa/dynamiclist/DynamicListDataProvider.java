package com.abinbev.dsa.dynamiclist;

import com.salesforce.dsa.data.model.SFBaseObject;

import java.util.List;

/**
 * Created by jstafanowski on 14.02.18.
 */

public interface DynamicListDataProvider<T extends SFBaseObject> {
    List<T> fetchRecords(String objectName, List<String> fieldNames, int pageStart, int pageSize);
}
