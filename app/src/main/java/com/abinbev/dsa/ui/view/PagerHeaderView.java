package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AdapterParameters;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 12/15/15.
 */
public class PagerHeaderView extends RelativeLayout {

    @Bind(R.id.page_title)
    TextView pageTitle;

    @Bind(R.id.page_indicators)
    LinearLayout pageIndicators;

    ViewPager viewPager;

    public PagerHeaderView(Context context) {
        this(context, null);
    }

    public PagerHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.negotiation_pager_header, this);
        ButterKnife.bind(this);
    }

    private void setViewPager(final ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < pageIndicators.getChildCount(); i++) {
                    pageIndicators.getChildAt(i).setActivated(i == position);
                }
                AdapterParameters adapter = (AdapterParameters)viewPager.getAdapter();
                pageTitle.setText(adapter.getName(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    viewPager.requestLayout();
                }
            }
        });
    }

    private void setNumberOfPages(int num) {
        pageIndicators.removeAllViews();
        for (int i = 0; i < num; i++) {
            ImageView indicator = new ImageView(getContext());
            indicator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            indicator.setImageResource(R.drawable.page_indicator);
            pageIndicators.addView(indicator);
        }
    }

    public void initHeader(ViewPager pager, int numOfPackages) {
        setViewPager(pager);
        setNumberOfPages(numOfPackages);
        pageIndicators.getChildAt(0).setActivated(true);
        AdapterParameters adapter = (AdapterParameters)pager.getAdapter();
        pageTitle.setText(adapter.getName(0));
    }

    @OnClick(R.id.page_left)
    public void pageLeft() {
        if (viewPager != null) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @OnClick(R.id.page_right)
    public void pageRight() {
        if (viewPager != null) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }
}
