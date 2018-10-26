package com.abinbev.dsa.dynamiclist;

import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;

/**
 * Created by jstafanowski on 14.02.18.
 */

public interface DynamicListItemRowBuilder {
    TableRow createListItemRow(LayoutInflater inflater, String fieldName, TableLayout parent);
}
