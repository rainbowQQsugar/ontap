package com.abinbev.dsa.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeChildrenAdapter;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.presenter.VolumeDetailsPresenter;
import com.abinbev.dsa.ui.presenter.VolumePresenter;
import com.abinbev.dsa.ui.view.VolumeChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by Jakub Stefanowski on 08.03.2017.
 */

public class VolumeDetailsFragment extends AppBaseFragment implements VolumePresenter.ViewModel,
        VolumeDetailsPresenter.ViewModel {

    private static final String ARG_ACCOUNT_ID = "account_id";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_SELECTED_CHILD_SOUP_ID = "selected_child_id";
    private static final String ARG_PARENT_KPI_NUM = "kpi_id";

    private static final String SAVED_STATE_SELECTED_CHILD_SOUP_ID = "state_selected_child";

    public interface OnVolumeDetailsCloseClickedListener {
        void onVolumeDetailsCloseClicked();
    }

    public interface ViewModel {
        void setHeaderTitle(String title);
        void setHeaderSubtitle(String subtitle);
    }

    @Bind(R.id.volume_list)
    ExpandedListView volumeListView;

    @Bind(R.id.volume_sales_days_label)
    TextView salesDayLabel;

    @Bind(R.id.volume_sales_days_hyphen)
    TextView salesDayHyphen;

    @Bind(R.id.volume_days_of_sale)
    TextView daysOfSaleTextView;

    @Bind(R.id.volume_name)
    TextView volumeNameTextView;

    @Bind(R.id.volume_chart)
    VolumeChartView volumeChart;

    @Bind(R.id.volume_children_list)
    ExpandedGridView volumeChildrenGrid;

    @Bind(R.id.volume_children_list_divider)
    View childrenListDivider;

    @Nullable
    @Bind(R.id.close_button)
    View closeButton;

    OnVolumeDetailsCloseClickedListener onCloseClickedListener;

    VolumeChildrenAdapter volumeChildrenAdapter;

    VolumeAdapter volumeAdapter;

    VolumePresenter volumePresenter;

    VolumeDetailsPresenter volumeDetailsPresenter;

    ViewModel fragmentViewModel;

    String accountId;

    String userId;

    String selectedChildSoupId;

    String parentKpiNum;

    boolean showCloseButton = true;

    public static VolumeDetailsFragment newAccountVolumeInstance(String accountId, String parentKpiNum, String selectedChildSoupId) {
        VolumeDetailsFragment fragment = new VolumeDetailsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_SELECTED_CHILD_SOUP_ID, selectedChildSoupId);
        args.putString(ARG_PARENT_KPI_NUM, parentKpiNum);
        fragment.setArguments(args);

        return fragment;
    }

    public static VolumeDetailsFragment newUserVolumeInstance(String userId, String parentKpiNum, String selectedKpiSoupId) {
        VolumeDetailsFragment fragment = new VolumeDetailsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_SELECTED_CHILD_SOUP_ID, selectedKpiSoupId);
        args.putString(ARG_PARENT_KPI_NUM, parentKpiNum);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountId = getArguments().getString(ARG_ACCOUNT_ID, null);
        userId = getArguments().getString(ARG_USER_ID, null);
        selectedChildSoupId = getArguments().getString(ARG_SELECTED_CHILD_SOUP_ID, null);
        parentKpiNum = getArguments().getString(ARG_PARENT_KPI_NUM, null);

        if (savedInstanceState != null) {
            selectedChildSoupId = savedInstanceState.getString(SAVED_STATE_SELECTED_CHILD_SOUP_ID, selectedChildSoupId);
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        volumeListView.setExpanded(true);
        volumeChildrenGrid.setExpanded(true);

        setVolumeChartVisible(false);
        setVolumeChildrenVisible(false);

        salesDayLabel.setVisibility(View.GONE);
        salesDayHyphen.setVisibility(View.GONE);

        if (closeButton != null) {
            closeButton.setVisibility(showCloseButton ? View.VISIBLE : View.GONE);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!TextUtils.isEmpty(accountId)) {
            volumePresenter = VolumePresenter.createAccountPresenter(accountId, parentKpiNum);
            volumeDetailsPresenter = VolumeDetailsPresenter.createAccountPresenter(selectedChildSoupId, accountId);
        }
        else if (!TextUtils.isEmpty(userId)) {
            volumePresenter = VolumePresenter.createUserPresenter(userId, parentKpiNum);
            volumeDetailsPresenter = VolumeDetailsPresenter.createUserPresenter(selectedChildSoupId, userId);
        }
        else {
            throw new IllegalStateException("UserId and AccountId cannot be both empty.");
        }

        volumeAdapter = new VolumeAdapter();
        volumeListView.setAdapter(volumeAdapter);

        volumeChildrenAdapter = new VolumeChildrenAdapter();
        volumeChildrenGrid.setAdapter(volumeChildrenAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        volumePresenter.setViewModel(this);
        volumePresenter.start();

        volumeDetailsPresenter.setViewModel(this);
        volumeDetailsPresenter.start();
    }

    @Override
    public void onStop() {
        volumePresenter.stop();
        volumeDetailsPresenter.stop();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_SELECTED_CHILD_SOUP_ID, selectedChildSoupId);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_volume_details;
    }

    @Override // VolumePresenter.ViewModel
    public void setCurrentKpis(List<KPI__c> kpis) {
        volumeAdapter.setData(kpis);
        volumeAdapter.setSelectedKpi(selectedChildSoupId);
        volumeAdapter.notifyDataSetChanged();
    }

    @Override // VolumePresenter.ViewModel
    public void setSaleDays(int currentDay, int maxDays) {
        salesDayLabel.setVisibility(View.VISIBLE);
        salesDayHyphen.setVisibility(View.VISIBLE);
        daysOfSaleTextView.setText(getString(R.string.days_of_sale, currentDay, maxDays));
    }

    @Override // VolumeDetailsPresenter.ViewModel
    public void setKpi(KPI__c kpi) {
        showSelectedKpi(kpi);
    }

    @Override // VolumeDetailsPresenter.ViewModel
    public void setHistoricalKpis(List<KPI__c> history) {
        setVolumeChartVisible(true);
        volumeChart.setKpis(history);
    }

    @Override // VolumeDetailsPresenter.ViewModel
    public void setChildrenKpis(List<KPI__c> childrenKpis) {
        setVolumeChildrenVisible(!childrenKpis.isEmpty());

        volumeChildrenAdapter.setItems(childrenKpis);
        volumeChildrenAdapter.notifyDataSetChanged();
    }

    @Override // VolumeDetailsPresenter.ViewModel
    public void setAccountName(String subtitle) {
        if (fragmentViewModel != null) {
            fragmentViewModel.setHeaderSubtitle(subtitle);
        }
    }

    @OnItemClick(R.id.volume_list)
    public void onKpiSelected(int position) {
        KPI__c kpi = volumeAdapter.getItem(position);
        selectedChildSoupId = kpi.getId();

        volumeAdapter.setSelectedKpi(selectedChildSoupId);
        volumeAdapter.notifyDataSetChanged();

        setVolumeChartVisible(false);
        setVolumeChildrenVisible(false);

        volumeDetailsPresenter.setKpiId(kpi.getId());
        volumeDetailsPresenter.start();

        showSelectedKpi(kpi);
    }

    @OnClick(R.id.close_button)
    public void onCloseClicked() {
        if (onCloseClickedListener != null) {
            onCloseClickedListener.onVolumeDetailsCloseClicked();
        }
    }

    public void setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
        if (closeButton != null) {
            closeButton.setVisibility(showCloseButton ? View.VISIBLE : View.GONE);
        }
    }

    public void setOnCloseClickedListener(OnVolumeDetailsCloseClickedListener onCloseClickedListener) {
        this.onCloseClickedListener = onCloseClickedListener;
    }

    private void showSelectedKpi(KPI__c kpi) {
        volumeNameTextView.setText(kpi.getKpiName());

        if (fragmentViewModel != null) {
            if (kpi != null) {
                fragmentViewModel.setHeaderTitle(getString(R.string.volume) + " – " + kpi.getKpiName());
            } else {
                fragmentViewModel.setHeaderTitle(null);
            }
        }
    }

    private void setVolumeChartVisible(boolean visible) {
        volumeChart.setVisibility(visible ? View.VISIBLE : View.GONE);

        boolean childrenGridVisible = volumeChildrenGrid.getVisibility() == View.VISIBLE;
        childrenListDivider.setVisibility(visible && childrenGridVisible ? View.VISIBLE : View.GONE);
    }

    private void setVolumeChildrenVisible(boolean visible) {
        volumeChildrenGrid.setVisibility(visible ? View.VISIBLE : View.GONE);

        boolean volumeChartVisible = volumeChart.getVisibility() == View.VISIBLE;
        childrenListDivider.setVisibility(visible && volumeChartVisible ? View.VISIBLE : View.GONE);
    }

    public void setFragmentViewModel(ViewModel fragmentViewModel) {
        this.fragmentViewModel = fragmentViewModel;
    }

    static class VolumeAdapter extends BaseAdapter {

        String selectedKpi;

        List<KPI__c> volumeList = new ArrayList<>();

        public void setData(List<KPI__c> volumes) {
            volumeList.clear();
            if (volumes != null) {
                volumeList.addAll(volumes);
            }
        }

        public void setSelectedKpi(String selectedKpi) {
            this.selectedKpi = selectedKpi;
        }

        @Override
        public int getCount() {
            return volumeList.size();
        }

        @Override
        public KPI__c getItem(int position) {
            return volumeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VolumeVH holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.volume_details_circular_list_item, parent, false);
                holder = new VolumeVH(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (VolumeVH) convertView.getTag();
            }

            KPI__c kpi = getItem(position);
            double actual = kpi.getActual();
            double target = kpi.getTarget();
            int percentProgress = target == 0 ? 0 : (int) ((actual / target) * 100);
            holder.name.setText(kpi.getKpiName());

            holder.progress.setProgress(percentProgress);
            holder.progress.setMax(100);
            holder.percent.setText(percentProgress + "%");
            holder.values.setText(String.format("%.0f%s / %.0f%s", actual, kpi.getTranslatedUnit(), target,
                    kpi.getTranslatedUnit()));

            if (Objects.equals(selectedKpi, kpi.getId())) {
                convertView.setBackgroundColor(Color.WHITE);
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }


            return convertView;
        }
    }

    static class VolumeVH {

        @Bind(R.id.volume_item_percent)
        TextView percent;

        @Bind(R.id.volume_item_progress)
        ProgressBar progress;

        @Bind(R.id.volume_item_name)
        TextView name;

        @Bind(R.id.volume_item_values)
        TextView values;

        public VolumeVH(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
