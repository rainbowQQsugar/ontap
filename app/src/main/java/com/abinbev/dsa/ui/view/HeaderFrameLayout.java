package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.abinbev.dsa.R;

/**
 * Created by wandersonblough on 11/16/15.
 */
public class HeaderFrameLayout extends FrameLayout {

    enum Mode {
        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3);

        private final int id;

        Mode(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }

        static Mode from(int id) {
            for (Mode mode : values()) {
                if (mode.getValue() == id) {
                    return mode;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    Paint paint;
    Path path;
    Mode mode;

    public HeaderFrameLayout(Context context) {
        this(context, null);
    }

    public HeaderFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HeaderFrameLayout);
        mode = Mode.from(ta.getInt(R.styleable.HeaderFrameLayout_layout_mode, 0));
        ta.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.black80));

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getMeasuredHeight();

        int offset = height / 2;
        Rect clipBounds = canvas.getClipBounds();

        int left = clipBounds.left;
        int right = clipBounds.right;
        int top = 0;

        switch (mode) {
            case TOP_LEFT:
                left += -offset;
                path.moveTo(left, top);
                path.lineTo(right, top);
                path.lineTo(right, top + height);
                path.lineTo(left + offset, top + height);
                path.lineTo(left, top);
                break;
            case TOP_RIGHT:
                right += offset;
                path.moveTo(left, top);
                path.lineTo(right, top);
                path.lineTo(right - offset, top + height);
                path.lineTo(left, top + height);
                path.lineTo(left, top);
                break;
            case BOTTOM_LEFT:
                left += -offset;
                path.moveTo(left, top);
                path.lineTo(right, top);
                path.lineTo(right, top + height);
                path.lineTo(left, top + height);
                path.lineTo(left + offset, top);
                break;
            case BOTTOM_RIGHT:
                right += offset;
                path.moveTo(left, top);
                path.lineTo(right - offset, top);
                path.lineTo(right, top + height);
                path.lineTo(left, top + height);
                path.lineTo(left, top);
                break;
            default:
                throw new IllegalArgumentException();
        }
        path.close();

        clipBounds.set(left, clipBounds.top, right, clipBounds.bottom);
        canvas.clipRect(clipBounds, Region.Op.REPLACE);

        canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }
}
