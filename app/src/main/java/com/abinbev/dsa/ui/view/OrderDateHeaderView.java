package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.abinbev.dsa.utils.DateUtils;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Diana Błaszczyk on 24/10/17.
 */

public class OrderDateHeaderView extends DateRangeHeaderView {

    private static final String TAG = OrderDateHeaderView.class.getSimpleName();

    private Subscription subscription;

    public OrderDateHeaderView(Context context) {
        this(context, null);
    }

    public OrderDateHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OrderDateHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        subscription = Subscriptions.empty();
    }

    @Override
    public void postStartDate(String dateString) {

    }

    @Override
    public void postEndDate(String dateString) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscription.unsubscribe();
    }
}
