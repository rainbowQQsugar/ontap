package com.abinbev.dsa.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.PieChartAdapter;
import com.abinbev.dsa.adapter.ScrollablePanelAdapter;
import com.abinbev.dsa.model.CN_KPI_Dict__c;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.customviews.ScrollablePanel;
import com.abinbev.dsa.ui.presenter.KpiDetailsPresenter;
import com.abinbev.dsa.ui.view.DateSelectorPopwindow;
import com.abinbev.dsa.ui.view.KpiProgressView;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.abinbev.dsa.activity.KpiDetailsActivity.ARGS_PARENT_KPI_ID;
import static com.abinbev.dsa.activity.VolumeDetailsActivity.ARGS_USER_ID;

public class PerformanceKpiFragment extends Fragment implements KpiDetailsPresenter.ViewModel, DateSelectorPopwindow.OnDateItemSelected {
    @Bind(R.id.left)
    FrameLayout left;
    @Bind(R.id.opted_date)
    TextView optedDate;
    @Bind(R.id.pull_down)
    ImageView pullDown;
    @Bind(R.id.right)
    FrameLayout right;
    @Bind(R.id.swfit_date)
    LinearLayout swfitDate;
    @Bind(R.id.kpi_pie_chart)
    ExpandedGridView kpiChartContainer;
    @Bind(R.id.data_chart)
    ScrollablePanel dataChart;
    @Bind(R.id.kpi_list_view)
    ExpandedListView listView;
    private String kpiID, userId;
    private KpiDetailsPresenter kpiDetailsPresenter;
    private User user;
    private ScrollablePanelAdapter scrollablePanelAdapter;
    private List<List<KPI__c>> allKpiList = new ArrayList<>();
    private List<List<KPI__c>> pieChartList = new ArrayList<>();
    private DateSelectorPopwindow dateSelectorPopwindow;
    private int currentDatePosition;
    public boolean shouldRefrehDatas;
    private String TAG = getClass().getSimpleName();
    private PieChartAdapter pieChartAdapter;

    public static PerformanceKpiFragment newInstance(String parentkpiId, String userId) {
        Bundle args = new Bundle();
        args.putString(ARGS_PARENT_KPI_ID, parentkpiId);
        args.putString(ARGS_USER_ID, userId);
        PerformanceKpiFragment fragment = new PerformanceKpiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_performance_api, container, false);
        ButterKnife.bind(this, view);
        kpiID = getArguments().getString(ARGS_PARENT_KPI_ID);
        userId = getArguments().getString(ARGS_USER_ID);
        kpiDetailsPresenter = new KpiDetailsPresenter(getActivity(), userId, kpiID);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        kpiDetailsPresenter.setViewModel(this);
        presentStart();
    }

    public void presentStart() {
        kpiDetailsPresenter.start();
    }

    @OnClick({R.id.left, R.id.swfit_date, R.id.right})
    public void onViewClicked(View view) {
        if (allKpiList == null || allKpiList.isEmpty() || allKpiList.get(0) == null
                || pieChartList == null || pieChartList.isEmpty()) return;
        switch (view.getId()) {
            case R.id.left:
                right.setVisibility(View.VISIBLE);
                if (currentDatePosition > allKpiList.size() - 1) {
                    currentDatePosition = allKpiList.size() - 1;
                    return;
                }
                List<KPI__c> c = allKpiList.get(currentDatePosition += 1);
                List<KPI__c> list = pieChartList.get(currentDatePosition );
                if (currentDatePosition == allKpiList.size() - 1)
                    left.setVisibility(View.INVISIBLE);
                triggerChange(c, list);
                break;
            case R.id.swfit_date:
                dateSelectorPopwindow = new DateSelectorPopwindow();
                dateSelectorPopwindow.buildPopwindow(getActivity());
                dateSelectorPopwindow.setKpis(allKpiList);
                dateSelectorPopwindow.showAsDropDown(swfitDate);
                dateSelectorPopwindow.setOnDateItemSelected(this);
                break;
            case R.id.right:
                left.setVisibility(View.VISIBLE);
                if (currentDatePosition < 0) {
                    currentDatePosition = 0;
                    return;
                }
                c = allKpiList.get(currentDatePosition -= 1);
                list = pieChartList.get(currentDatePosition);
                if (currentDatePosition == 0)
                    right.setVisibility(View.INVISIBLE);
                triggerChange(c, list);
                break;
        }
    }

    private void triggerChange(List<KPI__c> c, List<KPI__c> list) {
        if (c.isEmpty()) optedDate.setText("");
        else {
            String[] split = c.get(0).getEndDate().split("-");
            optedDate.setText(split.length > 2 ? split[0] + "-" + split[1] : "");
            setAdapter(c, list);
        }
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void setData(List<KPI__c> kpis) {
    }

    @Override
    public void setLastSixMonthData(KpiDetailsPresenter.FetchResult data) {
        if (allKpiList.isEmpty() || shouldRefrehDatas) {
            List<List<KPI__c>> kpiList = data.dataInTable;
            List<List<KPI__c>> pieChartdata = data.dataInPieChart;
            allKpiList.clear();
            pieChartList.clear();
            pieChartList.addAll(pieChartdata);
            allKpiList.addAll(kpiList);
            shouldRefrehDatas = false;
            if (kpiList == null || kpiList.isEmpty() || pieChartList == null || pieChartList.isEmpty())
                return;
            left.setVisibility(kpiList.size() < 3 ? View.INVISIBLE : View.VISIBLE);
            right.setVisibility(kpiList.size() > 1 ? View.VISIBLE : View.INVISIBLE);
            currentDatePosition = kpiList.size() > 1 ? 1 : 0;
            String[] split = kpiList.get(currentDatePosition).get(0).getEndDate().split("-");
            optedDate.setText(split.length > 2 ? split[0] + "-" + split[1] : "");
            setAdapter(kpiList.get(currentDatePosition), pieChartList.get(currentDatePosition));
        }
    }

    private void setAdapter(List<KPI__c> kpiList, List<KPI__c> pieChartList) {
        if (scrollablePanelAdapter == null)
            scrollablePanelAdapter = new ScrollablePanelAdapter();
        groupDatas(kpiList);
        dataChart.setPanelAdapter(scrollablePanelAdapter);
        if (pieChartAdapter == null) {
            pieChartAdapter = new PieChartAdapter(pieChartList);
            kpiChartContainer.setAdapter(pieChartAdapter);
        } else
            pieChartAdapter.setData(pieChartList);
    }

    @Override
    public void setDays(int currentDay, int maxDays) {
    }

    @Override
    public void setPerformanceKpis(List<CN_KPI_Statistic__c> kpiList) {
    }

    private void groupDatas(List<KPI__c> kpiList) {
        if (kpiList == null || kpiList.isEmpty()) return;
        String[] firstRowValues = new String[]{getActivity().getString(R.string.target),
                getActivity().getString(R.string.accomplishment), getActivity().getString(R.string.achieving_rate)};
        List<List<String>> contentValues = new ArrayList<>();

        ArrayList<String> kpiNames = new ArrayList<>();
        ArrayList<String> targets = new ArrayList<>();
        ArrayList<String> actuals = new ArrayList<>();
        ArrayList<String> percents = new ArrayList<>();

        for (KPI__c kpi__c : kpiList) {
            String externalId = kpi__c.getKpiName();
            String kpiName = CN_KPI_Dict__c.getKpiNameByExternalId(externalId);
            kpiNames.add(kpiName);
            targets.add(kpi__c.getTarget() + "");
            actuals.add(kpi__c.getActual() + "");
            percents.add(kpi__c.getPercentCompleted());
        }

        contentValues.add(targets);
        contentValues.add(actuals);
        contentValues.add(percents);
        scrollablePanelAdapter.setFirstColume(kpiNames);
        scrollablePanelAdapter.setValues(firstRowValues);
        scrollablePanelAdapter.setContentValues(contentValues);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDateItemSelected(List<KPI__c> c, int pos) {
        if (c.isEmpty()) return;
        String[] split = c.get(0).getEndDate().split("-");
        optedDate.setText(split.length > 2 ? split[0] + "-" + split[1] : "");
        if (dateSelectorPopwindow.isShowing()) dateSelectorPopwindow.dismiss();
        setAdapter(c, pieChartList.get(pos));
        currentDatePosition = pos;
        if (pos == 0) {
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.INVISIBLE);
        } else if (pos == allKpiList.size() - 1) {
            right.setVisibility(View.VISIBLE);
            left.setVisibility(View.INVISIBLE);
        } else {
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.VISIBLE);
        }
        if (allKpiList.size() == 1) {
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
        }
    }
}
