package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.abinbev.dsa.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KPIDetailChartView extends View {
    private static final float MARGIN_PERCENT = 0.25f;
    private static final String ITEM_LABEL = "label";
    private static final String ITEM_VALUE = "value";
    private static final String ITEM_COLOR = "color";
    private static final String ITEM_PERCENT = "percent";
    private List<Map<String, Object>> items;
    private float totalValue;
    private int textSize;
    private int textPadding;
    private Paint paint;


    public KPIDetailChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        items = new ArrayList<>();
        totalValue = 0;
        textSize = ViewUtils.getDipsFromPixel(12, getContext());
        textPadding = ViewUtils.getDipsFromPixel(4, getContext());
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);

    }

    public void addItem(String label, Integer value, int color) {
        Map<String, Object> map = new HashMap<>();
        map.put(ITEM_LABEL, label);
        map.put(ITEM_VALUE, value);
        map.put(ITEM_COLOR, color);
        items.add(map);
        totalValue += value;
    }


    public void clear() {
        items.clear();
        totalValue = 0;
        postInvalidate();
    }

    public void prepare() {
        for (Map<String, Object> item : items) {
            Integer value = (Integer) item.get(ITEM_VALUE);
            Float percent = value * 1.0f / totalValue;
            item.put(ITEM_PERCENT, percent);
        }
        postInvalidate();
    }

    private void drawItem(Canvas canvas, String label, int color, float width, float height, float percent, float totalDisplayPercent) {
        canvas.save();
        paint.setColor(color);
        Path path = new Path();
        path.moveTo(width * MARGIN_PERCENT, height);
        path.lineTo(width * (1 - MARGIN_PERCENT), height);
        path.lineTo(width, 0);
        path.lineTo(0, 0);
        path.close();
        float bottom = height * totalDisplayPercent;
        float top = bottom - height * percent;
        canvas.clipRect(0, top, width, bottom);
        paint.setAlpha(255);
        canvas.drawPath(path, paint);
        paint.setAlpha(128);
        LinearGradient linearGradient = new LinearGradient(
                0, height/2,
                width/2, height/2,
                color, Color.WHITE,
                Shader.TileMode.MIRROR
        );
        paint.setShader(linearGradient);
        canvas.drawPath(path,paint);
        paint.setShader(null);
        canvas.restore();
        //Draw Label
        float centerX = width / 2;
        float centerY = (top + bottom) / 2;
        float stringWidth = paint.measureText(label);
        paint.setColor(Color.WHITE);
        paint.setAlpha(200);
        canvas.drawRect(centerX - stringWidth / 2 - textPadding,
                centerY - textSize / 2 - textPadding,
                centerX + stringWidth / 2 + textPadding,
                centerY + textSize / 2 + textPadding,
                paint);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float offsetY = (Math.abs(fontMetrics.ascent) - fontMetrics.descent) / 2;
        canvas.drawText(label, centerX - stringWidth / 2, centerY + offsetY, paint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float totalDisplayPercent = 0;
        for (int i = 0; i < items.size(); i++) {
            Map item = items.get(i);
            String label = (String) item.get(ITEM_LABEL);
            Float percent = (Float) item.get(ITEM_PERCENT);
            Integer color = (Integer) item.get(ITEM_COLOR);
            totalDisplayPercent += percent;
            drawItem(canvas, label, color, width, height, percent, totalDisplayPercent);
        }
    }

}
