package com.abinbev.dsa.ui.decorator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.TimelineAdapter;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by mewa on 6/21/17.
 */

public class TimelineDecorator extends RecyclerView.ItemDecoration {
    private final int offset;
    private final int itemOffset;
    private final int timelineWidth;
    private final Map<Integer, Drawable> drawableMap = new WeakHashMap<>(6);
    private final int iconSize;
    private final boolean split;

    public TimelineDecorator(int offset, int itemOffset, int timelineWidth, int iconSize, boolean split) {
        this.offset = offset;
        this.itemOffset = itemOffset;
        this.timelineWidth = timelineWidth;
        this.iconSize = iconSize;
        this.split = split;
    }

    public TimelineDecorator(Context themedContext, boolean split) {
        this(
                Math.round(themedContext.getResources().getDisplayMetrics().density * 16),
                Math.round(themedContext.getResources().getDisplayMetrics().density * 32),
                Math.round(themedContext.getResources().getDisplayMetrics().density * 2),
                Math.round(themedContext.getResources().getDisplayMetrics().density * 48),
                split
        );
    }

    protected Drawable getDrawable(Context context, int viewType) {
        Drawable drawable = drawableMap.get(viewType);
        if (drawable == null) {
            switch (viewType) {
                case TimelineAdapter.NOTE_VIEW_TYPE:
                    drawable = context.getResources().getDrawable(R.drawable.ic_notes_circle, context.getTheme());
                    break;
                case TimelineAdapter.VISITS_VIEW_TYPE:
                    drawable = context.getResources().getDrawable(R.drawable.ic_visited_circle, context.getTheme());
                    break;
            }
            drawableMap.put(viewType, drawable);
        }
        return drawable;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        if (parent.getChildCount() > 0) {

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(parent.getResources().getColor(R.color.divider_color));
            paint.setStrokeWidth(timelineWidth);

            int baseOffset;

            if (split) {
                baseOffset = iconSize;
                int mid = Math.round((parent.getWidth() - baseOffset) / 2f);
                drawTimelineAtPosition(c, parent, paint, mid);
            } else {
                baseOffset = parent.getPaddingLeft();
                drawTimelineAtPosition(c, parent, paint, baseOffset);
            }
        }
    }

    private void drawTimelineAtPosition(Canvas c, RecyclerView parent, Paint paint, int x) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        int top, bottom, left, right, mid;
        bottom = 0;
        left = x;
        right = left + iconSize;
        mid = Math.round((left + right) / 2f);

        for (int i = 0; i < layoutManager.getChildCount(); ++i) {
            View child = layoutManager.getChildAt(i);

            top = child.getTop();
            bottom = top + iconSize;

            Drawable drawable = getDrawable(parent.getContext(), parent.getChildViewHolder(child).getItemViewType());
            if (drawable != null) {
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(c);
            } else {
                c.drawOval(left, top, right, bottom, paint);
            }
            // Draw vertical line
            top = child.getTop() + iconSize + 2 * timelineWidth;
            bottom = child.getBottom() + itemOffset - 2 * timelineWidth;

            c.drawLine(mid, top, mid, bottom, paint);
        }
        if (bottom < parent.getBottom()) {
            c.drawLine(mid, bottom, mid, parent.getBottom(), paint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int baseOffset;
        outRect.bottom = itemOffset;

        if (split) {
            int halfIconSize = Math.round(iconSize / 2f);
            int mid = Math.round(parent.getWidth() / 2f);
            int pos = parent.getChildAdapterPosition(view);

            baseOffset = mid + halfIconSize;

            if (pos % 2 == 0) {
                outRect.left = baseOffset;
            } else {
                outRect.right = baseOffset;
            }
        } else {
            baseOffset = iconSize + parent.getPaddingLeft();
            outRect.left = baseOffset;
        }
    }
}
