/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Le Van Hoang
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/


package com.salesforce.dsa.app.ui.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.salesforce.dsa.app.R;

public class SegmentedGroup extends RadioGroup {

    private int oneDP;
    private Resources resources;
    private int mTintColor;
    private int mCheckedTextColor = Color.WHITE;

    public SegmentedGroup(Context context) {
        super(context);
        resources = getResources();
        mTintColor = resources.getColor(R.color.accent);
        oneDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, resources.getDisplayMetrics());
    }

    public SegmentedGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        resources = getResources();
        mTintColor = resources.getColor(R.color.accent);
        oneDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, resources.getDisplayMetrics());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Use holo light for default
        updateBackground();
    }

    public void setTintColor(int tintColor) {
        mTintColor = tintColor;
        updateBackground();
    }

    public void setTintColor(int tintColor, int checkedTextColor) {
        mTintColor = tintColor;
        mCheckedTextColor = checkedTextColor;
        updateBackground();
    }

    public void updateBackground() {
        int count = super.getChildCount();
        if (count > 1) {
            View child = getChildAt(0);
            LayoutParams initParams = (LayoutParams) child.getLayoutParams();
            LayoutParams params = new LayoutParams(initParams.width, initParams.height, initParams.weight);
            params.setMargins(0, 0, -oneDP, 0);
            child.setLayoutParams(params);
            updateBackground(getChildAt(0), R.drawable.radio_checked_left, R.drawable.radio_unchecked_left);
            for (int i = 1; i < count - 1; i++) {
                updateBackground(getChildAt(i), R.drawable.radio_checked_middle, R.drawable.radio_unchecked_middle);
                View child2 = getChildAt(i);
                initParams = (LayoutParams) child2.getLayoutParams();
                params = new LayoutParams(initParams.width, initParams.height, initParams.weight);
                params.setMargins(0, 0, -oneDP, 0);
                child2.setLayoutParams(params);
            }
            updateBackground(getChildAt(count - 1), R.drawable.radio_checked_right, R.drawable.radio_unchecked_right);
        } else if (count == 1) {
            updateBackground(getChildAt(0), R.drawable.radio_checked_default, R.drawable.radio_unchecked_default);
        }
    }

    private void updateBackground(View view, int checked, int unchecked) {
        //Set text color
        ColorStateList colorStateList = new ColorStateList(new int[][]{
                {android.R.attr.state_pressed},
                {-android.R.attr.state_pressed, -android.R.attr.state_checked},
                {-android.R.attr.state_pressed, android.R.attr.state_checked}},
                new int[]{Color.GRAY, mTintColor, mCheckedTextColor});
        ((Button) view).setTextColor(colorStateList);

        //Redraw with tint color
        Drawable checkedDrawable = resources.getDrawable(checked).mutate();
        Drawable uncheckedDrawable = resources.getDrawable(unchecked).mutate();
        ((GradientDrawable) checkedDrawable).setColor(mTintColor);
        ((GradientDrawable) uncheckedDrawable).setStroke(oneDP, mTintColor);

        //Create drawable
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, uncheckedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, checkedDrawable);

        //Set button background
        view.setBackground(stateListDrawable);
    }
}
