package com.abinbev.dsa.dynamiclist;

import android.text.TextUtils;

import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import java.util.List;
import java.util.Locale;

/**
 * Created by jstafanowski on 14.02.18.
 */

public class SimpleDataProvider<T extends SFBaseObject> implements DynamicListDataProvider<T> {

    private final Class<T> objectClass;

    private String queryFilter;

    public SimpleDataProvider(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    public SimpleDataProvider<T> setQueryFilter(String queryFilter) {
        this.queryFilter = queryFilter;
        return this;
    }

    @Override
    public List<T> fetchRecords(String objectName, List<String> fieldNames, int pageStart, int pageSize) {
        String query = String.format(Locale.US, "SELECT {%1$s:_soup} FROM {%1$s}", objectName);
        if (!TextUtils.isEmpty(queryFilter)) {
            query += " WHERE " + queryFilter;
        }

        query += String.format(Locale.US, " LIMIT %d OFFSET %d", pageSize, pageStart);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, query, objectClass);
    }
}
