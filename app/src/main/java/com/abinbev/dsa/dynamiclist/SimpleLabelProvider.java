package com.abinbev.dsa.dynamiclist;

import android.content.Context;
import android.support.annotation.StringRes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jstafanowski on 14.02.18.
 */

public class SimpleLabelProvider implements DynamicListFieldLabelProvider {

    private final Map<String, Object> labelsMap = new HashMap<>();

    public SimpleLabelProvider addLabel(String fieldName, String label) {
        labelsMap.put(fieldName, label);
        return this;
    }

    public SimpleLabelProvider addLabel(String fieldName, @StringRes int label) {
        labelsMap.put(fieldName, label);
        return this;
    }

    @Override
    public Map<String, String> getFieldLabels(Context context, String objectName, List<String> fieldNames) {
        HashMap<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : labelsMap.entrySet()) {
            String fieldName = entry.getKey();
            String label = getText(context, entry.getValue());

            result.put(fieldName, label);
        }

        return result;
    }

    private String getText(Context context, Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Integer) {
            return context.getString((Integer) value);
        }
        else {
            return null;
        }
    }
}
