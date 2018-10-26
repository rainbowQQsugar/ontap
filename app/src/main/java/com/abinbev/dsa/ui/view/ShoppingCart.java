package com.abinbev.dsa.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class ShoppingCart extends LinearLayout {
    @Bind(R.id.txtItemCount)
    TextView itemCount;

    @Bind(R.id.txtLabel)
    TextView label;

    public ShoppingCart(Context context) {
        super(context);
        init(context, null);
    }

    public ShoppingCart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShoppingCart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public ShoppingCart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.merge_shopping_cart, this);
        setOrientation(LinearLayout.HORIZONTAL);
        ButterKnife.bind(this);

        setupBackground(context);
        setClickable(true);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ShoppingCart,
                0, 0);
        try {
            CharSequence text = typedArray.getText(R.styleable.ShoppingCart_cartTitle);
            label.setText(text);
        } finally {
            typedArray.recycle();
        }
    }

    private void setupBackground(Context context) {
        // set the background to whatever the selectable item background is for our theme
        int[] attr = new int[]{ R.attr.selectableItemBackgroundBorderless };
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        try {
            int backgroundResource = typedArray.getResourceId(0, 0);
            setBackgroundResource(backgroundResource);
        } finally {
            typedArray.recycle();
        }
    }

    public void setCount(int count) {
        itemCount.setText(String.valueOf(count));
    }
}
