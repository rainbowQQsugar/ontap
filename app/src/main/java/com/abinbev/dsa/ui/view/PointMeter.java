package com.abinbev.dsa.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.abinbev.dsa.R;

/**
 * Created by wandersonblough on 1/11/16.
 */
public class PointMeter extends View {

    Paint meterPaint;
    Paint goalPaint;
    Paint higherLimitPaint;
    Paint centerPaint;
    Paint handlePaint;
    RectF rectF;
    float strokeWidth;
    int diff = 0;
    int topMargin;

    private static final int SECTION_ANGLE = 60;
    private float lowerLimit;
    private float higherLimit;

    public PointMeter(Context context) {
        this(context, null);
    }

    public PointMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidth = getResources().getDimensionPixelSize(R.dimen.meter_width);

        meterPaint = new Paint();
        meterPaint.setColor(getResources().getColor(R.color.abi_green));
        meterPaint.setStrokeWidth(strokeWidth);
        meterPaint.setAntiAlias(true);
        meterPaint.setStyle(Paint.Style.STROKE);

        goalPaint = new Paint();
        goalPaint.setColor(getResources().getColor(R.color.sab_yellow));
        goalPaint.setStrokeWidth(strokeWidth);
        goalPaint.setAntiAlias(true);
        goalPaint.setStyle(Paint.Style.STROKE);

        higherLimitPaint = new Paint();
        higherLimitPaint.setColor(getResources().getColor(R.color.red));
        higherLimitPaint.setStrokeWidth(strokeWidth);
        higherLimitPaint.setAntiAlias(true);
        higherLimitPaint.setStyle(Paint.Style.STROKE);

        centerPaint = new Paint();
        centerPaint.setColor(getResources().getColor(R.color.sab_black));
        centerPaint.setAntiAlias(true);

        handlePaint = new Paint();
        handlePaint.setColor(getResources().getColor(R.color.sab_black));
        handlePaint.setAntiAlias(true);
        handlePaint.setStrokeCap(Paint.Cap.ROUND);
        handlePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.handle_width));

        topMargin = attrs.getAttributeIntValue("", "meter_top_margin", (int) strokeWidth);

        rectF = new RectF();
    }

    public void setLimits(int lowerLimit, int higherLimit) {
        this.lowerLimit = lowerLimit;
        this.higherLimit = higherLimit;
    }

    public void setPesos(int pesos) {
        ValueAnimator handleAnimator = ValueAnimator.ofInt(diff, pesos);
        handleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                diff = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        handleAnimator.start();
    }

    public void setGivesGets(int numOfGives, int numOfGets) {
        ValueAnimator handleAnimator = ValueAnimator.ofInt(diff, numOfGets - numOfGives);
        handleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                diff = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        handleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float top = (topMargin + (strokeWidth / 2));
        float left = strokeWidth / 2;
        float right = getWidth() - left;
        float bottom = (2 * (getHeight() - topMargin)) - (strokeWidth / 2);

        //draw meter
        rectF.set(left, top, right, bottom);
        canvas.drawCircle(getWidth() / 2, getHeight(), (strokeWidth / 3), centerPaint);

        canvas.drawArc(rectF, 180, SECTION_ANGLE, false, meterPaint);
        canvas.drawArc(rectF, 180 + SECTION_ANGLE, SECTION_ANGLE, false, goalPaint);
        canvas.drawArc(rectF, 180 + 2*SECTION_ANGLE, SECTION_ANGLE, false, higherLimitPaint);

//        float lowerLimitAngle = (lowerLimit/999999f)*180;
//        float higherLimitAngle =  ((upperLimit - lowerLimit)/999999f) * 180;
//        canvas.drawArc(rectF, 180, lowerLimitAngle, false, meterPaint);
//        canvas.drawArc(rectF, 180 + lowerLimitAngle, higherLimitAngle, false, goalPaint);
//        canvas.drawArc(rectF, 180 + lowerLimitAngle + higherLimitAngle, 180 - (higherLimitAngle + lowerLimitAngle), false, higherLimitPaint)

        //draw handle
        float handleLength = (getHeight() - (top + strokeWidth));
        double handleAngle = getAngle(diff);
        int dx = (int) (Math.cos(handleAngle) * handleLength);
        int dy = (int) (Math.sin(handleAngle) * handleLength);

        int x = diff < 0 ? (getWidth() / 2) - dx : (getWidth() / 2) + dx;
        int y = getHeight() + dy;
        canvas.drawLine(x, y, getWidth() / 2, getHeight(), handlePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = (int) (height + (1f * height));
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height + topMargin, MeasureSpec.EXACTLY));
    }

    private double getAngle(int diff) {
        double angle = 0;
        if (diff < lowerLimit) {
            angle = 180 + ((diff/lowerLimit)*SECTION_ANGLE);
        } else if (diff > higherLimit) {
            angle = 180 + 2*SECTION_ANGLE + (SECTION_ANGLE/2);
        } else {
            angle = 180 + SECTION_ANGLE + ((diff-lowerLimit)/(higherLimit - lowerLimit))*SECTION_ANGLE;
        }

        return Math.toRadians(angle);
    }
}
