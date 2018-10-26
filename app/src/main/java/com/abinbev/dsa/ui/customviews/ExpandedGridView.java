package com.abinbev.dsa.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

public class ExpandedGridView extends GridView {

    boolean expanded = true;

    public ExpandedGridView(Context context) {
        super(context);
    }

    public ExpandedGridView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ExpandedGridView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isExpanded())
        {

            // Option 1
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.

            // int expandSpec = MeasureSpec.makeMeasureSpec(
            //        Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);


            // Option 2
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                    MeasureSpec.AT_MOST);

            super.onMeasure(widthMeasureSpec, expandSpec);

            // is the below necessary?
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}

