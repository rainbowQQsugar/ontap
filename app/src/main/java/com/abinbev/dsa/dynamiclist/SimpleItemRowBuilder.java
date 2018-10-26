package com.abinbev.dsa.dynamiclist;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;

/**
 * Created by jstafanowski on 14.02.18.
 */

public class SimpleItemRowBuilder implements DynamicListItemRowBuilder {

    @LayoutRes
    private final int rowLayoutRes;

    public SimpleItemRowBuilder(@LayoutRes int rowLayoutRes) {
        this.rowLayoutRes = rowLayoutRes;
    }

    @Override
    public TableRow createListItemRow(LayoutInflater inflater, String fieldName, TableLayout parent) {
        return (TableRow) inflater.inflate(rowLayoutRes, parent, false);
    }
}
