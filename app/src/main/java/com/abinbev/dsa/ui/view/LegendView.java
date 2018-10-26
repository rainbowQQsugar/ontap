package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 12/1/15.
 */
public class LegendView extends LinearLayout {

    @Bind(R.id.legend_handle)
    ImageView legendHandle;

    @Bind(R.id.key_container)
    LinearLayout keyContainer;

    boolean keyShown;

    public LegendView(Context context) {
        this(context, null);
    }

    public LegendView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LegendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.merge_legend, this);
        ButterKnife.bind(this);
        setupKeys();
    }

    private void setupKeys() {
        keyContainer.removeAllViews();
        TextView[] keys = new TextView[4];
        keys[0] = createKey(getContext().getString(R.string.to_visit), R.color.normal);
        keys[1] = createKey(getContext().getString(R.string.visited), R.color.visited);
        keys[2] = createKey(getContext().getString(R.string.selected), R.color.selected);
        keys[3] = createKey(getContext().getString(R.string.checked_in), R.color.checked_in);
        for (TextView key : keys) {
            keyContainer.addView(key);
        }
    }

    @OnClick(R.id.legend_handle)
    public void toggle() {
        keyShown = !keyShown;
        keyContainer.setVisibility(keyShown ? VISIBLE : INVISIBLE);
        legendHandle.setImageResource(keyShown ? R.drawable.ic_close : R.drawable.ic_help_black);
    }

    public TextView createKey(String label, int colorResId) {
        TextView textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_h5));
        textView.setText(label);
        textView.setPadding(4, 4, 4, 4);
        textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Drawable icon = getResources().getDrawable(R.drawable.key_square);
        if (icon != null) {
            icon.mutate().setColorFilter(getResources().getColor(colorResId), PorterDuff.Mode.SRC_ATOP);
        }
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        textView.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.space));
        return textView;
    }


}
