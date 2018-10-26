package com.abinbev.dsa.ui.customviews;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 08.08.2017.
 */

public class VisitListTabBar extends FrameLayout {

    public static final int TAB_IN_PLAN_VISITS = 0;

    public static final int TAB_OUT_OF_PLAN_VISITS = 1;

    public static final int TAB_ADD = 2;

    public static final int TAB_FILTERS = 3;

    public interface OnTabSelectedListener {
        void onTabSelected(int tab);
    }

    public interface OnFilterClickedListener {
        void onFilterClicked();
    }

    @Bind(R.id.visit_tab_bar_txt_in_plan_visits)
    TextView txtInPlanVisitCount;

    @Bind(R.id.visit_tab_bar_txt_out_of_plan_visits)
    TextView txtOutOfPlanVisitCount;

    @Bind({
            R.id.visit_tab_bar_button_in_plan_visits,
            R.id.visit_tab_bar_button_out_of_plan_visits,
            R.id.visit_tab_bar_button_add,
            R.id.visit_tab_bar_button_filter
    })
    List<View> tabs;

    private int selectedTab;

    private OnTabSelectedListener listener;

    private OnFilterClickedListener onFilterClickedListener;

    public VisitListTabBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VisitListTabBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VisitListTabBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_visit_list_tab_bar, this);
        ButterKnife.bind(this);

        setInPlanVisitsCount(0, 0);
        setOutOfPlanVisitsCount(0, 0);
    }

    public void setInPlanVisitsCount(int completed, int total) {
        txtInPlanVisitCount.setText(completed + "/" + total);
    }

    public void setOutOfPlanVisitsCount(int completed, int total) {
        txtOutOfPlanVisitCount.setText(completed + "/" + total);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setOnFilterClickedListener(OnFilterClickedListener l) {
        this.onFilterClickedListener = l;
    }

    public void changeSelectedTab(int tab) {
        showAsSelected(tabs.get(tab));

        if (listener != null) {
            listener.onTabSelected(selectedTab);
        }
    }

    public void setSelectedTab(int tab) {
        showAsSelected(tabs.get(tab));
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    @OnClick({
            R.id.visit_tab_bar_button_in_plan_visits,
            R.id.visit_tab_bar_button_out_of_plan_visits,
            R.id.visit_tab_bar_button_add,
            R.id.visit_tab_bar_button_filter
    })
    void onTabClicked(View clickedTab) {
        showAsSelected(clickedTab);

        if (listener != null) {
            listener.onTabSelected(selectedTab);
        }
    }

    @OnClick(R.id.visit_tab_bar_button_filter)
    void onFilterClickedListener() {
        if (onFilterClickedListener != null) {
            onFilterClickedListener.onFilterClicked();
        }
    }

    private void showAsSelected(View view) {
        for (int i = 0; i < tabs.size(); i++) {
            View currentTab = tabs.get(i);
            if (view == currentTab) {
                selectedTab = i;
                currentTab.setActivated(true);
            }
            else {
                currentTab.setActivated(false);
            }
        }
    }
}
