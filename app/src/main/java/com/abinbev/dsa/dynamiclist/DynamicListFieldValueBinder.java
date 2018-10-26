package com.abinbev.dsa.dynamiclist;

import android.view.View;

import com.salesforce.dsa.data.model.SFBaseObject;

/**
 * Created by jstafanowski on 14.02.18.
 */

public interface DynamicListFieldValueBinder {
    void bindValue(String fieldName, View view, SFBaseObject data);
}
