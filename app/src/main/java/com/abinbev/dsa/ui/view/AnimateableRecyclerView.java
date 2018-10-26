package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class AnimateableRecyclerView extends RecyclerView {
    public AnimateableRecyclerView(Context context) {
        super(context);
    }

    public AnimateableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimateableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void scrollTo(int x, int y) {
        // do nothing
    }

}
