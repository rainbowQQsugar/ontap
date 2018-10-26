package com.abinbev.dsa.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.abinbev.dsa.R;

/**
 * Created by Jakub Stefanowski on 06.02.2017.
 */

public class DiagonalLineView extends View {

    private int color = Color.BLACK;

    private float strokeWidth = 4;

    private Paint paint;

    public DiagonalLineView(Context context) {
        super(context);
        setup(context, null);
    }

    public DiagonalLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public DiagonalLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.DiagonalLineView,
                    0, 0);

            try {
                color = a.getColor(R.styleable.DiagonalLineView_lineColor, Color.BLACK);
                strokeWidth = a.getDimension(R.styleable.DiagonalLineView_lineSize, 4);
            } finally {
                a.recycle();
            }
        }

        this.paint = new Paint();
        this.paint.setColor(color);
        this.paint.setStrokeWidth(strokeWidth);
        this.paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        float strokeWidthHalf = strokeWidth / 2f;

        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawLine(getPaddingLeft() + width - strokeWidthHalf,
                getPaddingTop() + strokeWidthHalf,
                getPaddingLeft() + strokeWidthHalf,
                getPaddingTop() + height - strokeWidthHalf,
                paint);
    }
}
