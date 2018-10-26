package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.abinbev.dsa.R;

/**
 * Created by nchangnon on 12/9/15.
 */
public class SortableHeader extends TextView {

    Boolean sortAscending;

    public SortableHeader(Context context) {
        super(context);
    }

    public SortableHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SortableHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Toggles the sort direction displayed. If the field was not previously
     * displaying a sort direction, ascending is chosen.
     *
     * @return Boolean indicating if ascending or not (descending). Null means null, not sorted.
     */
    public Boolean toggleSortDirection() {
        if (sortAscending == null) {
            sortAscending = true;
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sort_asc, 0, 0, 0);
        } else {
            sortAscending = !sortAscending;
            setCompoundDrawablesWithIntrinsicBounds(sortAscending ? R.drawable.ic_sort_asc : R.drawable.ic_sort_desc, 0, 0, 0);
        }

        return sortAscending;
    }

    public void sortAscending() {
        sortAscending = true;
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sort_asc, 0, 0, 0);
    }

    public void sortDescending() {
        sortAscending = false;
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sort_desc, 0, 0, 0);
    }

    public void clearSortIndicator() {
        sortAscending = null;
        setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
    }

}
