package com.abinbev.dsa.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import java.util.List;
import java.util.Objects;

/**
 * Created by jastef on 19.03.2018.
 */

public class PicklistValueAdapter extends ArrayAdapter<PicklistValue> {

    public PicklistValueAdapter(@NonNull final Context context, final int resource) {
        super(context, resource);
    }

    public PicklistValueAdapter(@NonNull final Context context, final int resource, final int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public PicklistValueAdapter(@NonNull final Context context, final int resource,
            @NonNull final PicklistValue[] objects) {
        super(context, resource, objects);
    }

    public PicklistValueAdapter(@NonNull final Context context, final int resource, final int textViewResourceId,
            @NonNull final PicklistValue[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public PicklistValueAdapter(@NonNull final Context context, final int resource,
            @NonNull final List<PicklistValue> objects) {
        super(context, resource, objects);
    }

    public PicklistValueAdapter(@NonNull final Context context, final int resource, final int textViewResourceId,
            @NonNull final List<PicklistValue> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public int getPositionByValue(String value) {
        for (int i = 0; i < getCount(); i++) {
            if (Objects.equals(value, getItem(i).getValue())) {
                return i;
            }
        }

        return -1;
    }
}
