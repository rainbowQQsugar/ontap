package com.abinbev.dsa.dynamiclist;

import android.view.View;
import android.widget.TextView;

import com.salesforce.dsa.data.model.SFBaseObject;

/**
 * Created by jstafanowski on 14.02.18.
 */

public class SimpleValueBinder implements DynamicListFieldValueBinder {

    @Override
    public void bindValue(String fieldName, View view, SFBaseObject data) {
        TextView textView = (TextView) view;
        textView.setText(data.getStringValueForKey(fieldName));
    }
}
