package com.abinbev.dsa.dynamiclist;

import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * Created by jstafanowski on 14.02.18.
 */

public interface DynamicListFieldLabelProvider {
    Map<String, String> getFieldLabels(Context context, String objectName, List<String> fieldNames);
}
