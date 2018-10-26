package com.abinbev.dsa.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeProgressAdapter;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.presenter.KpiDetailsPresenter;
import com.abinbev.dsa.utils.DateUtils;
import com.google.android.gms.common.data.DataHolder;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserKpiDetailsFragment extends Fragment implements KpiDetailsPresenter.ViewModel, AdapterView.OnItemClickListener {

    public static final String ARGS_PARENT_KPI_ID = "kpi_id";
    public static final String ARGS_USER_ID = "user_id";
    public static final String TAG = UserKpiDetailsFragment.class.getSimpleName();
    public static String ARGS_PARENT_CATEGORY_NAME = "category_name";

    @Bind(R.id.kpi_label)
    TextView titleTextView;

    @Bind(R.id.kpi_secondary_label)
    TextView subtitleTextView;

    @Bind(R.id.kpi_list)
    ExpandedGridView gridView;

    private KpiDetailsPresenter kpiDetailsPresenter;

    private VolumeProgressAdapter volumeAdapter;

    private String kpiID;

    private String userId;

    public static UserKpiDetailsFragment newInstance(String parentkpiId, String userId, String categoryName) {
        Bundle args = new Bundle();
        args.putString(ARGS_PARENT_KPI_ID, parentkpiId);
        args.putString(ARGS_USER_ID, userId);
        args.putString(ARGS_PARENT_CATEGORY_NAME, categoryName);
        UserKpiDetailsFragment fragment = new UserKpiDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_kpi_details, container, false);
        ButterKnife.bind(this, view);
        kpiID = getArguments().getString(ARGS_PARENT_KPI_ID);
        userId = getArguments().getString(ARGS_USER_ID);
        final String categoryName = getArguments().getString(ARGS_PARENT_CATEGORY_NAME);
        titleTextView.setText(categoryName);
        gridView.setExpanded(true);
        gridView.setOnItemClickListener(this);
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

    @Override
    public void onStop() {
        kpiDetailsPresenter.stop();

        super.onStop();
    }

    @Override
    public void setData(List<KPI__c> kpis) {
        if (volumeAdapter == null) {
            volumeAdapter = new VolumeProgressAdapter();
            gridView.setAdapter(volumeAdapter);
        }
        volumeAdapter.setData(kpis);
        volumeAdapter.notifyDataSetChanged();
    }

    @Override
    public void setLastSixMonthData(KpiDetailsPresenter.FetchResult allKpis) {

    }

    @Override
    public void setDays(int currentDay, int maxDays) {
    }

    @Override
    public void setPerformanceKpis(List<CN_KPI_Statistic__c> kpiList) {
        subtitleTextView.setText(getString(R.string.up_to_now) + " " + ((kpiList == null || kpiList.isEmpty() || kpiList.get(0) == null) ? "" : kpiList.get(0).geteDateValue()));
    }

    @Override
    public void setUser(User user) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        kpiDetailsPresenter.onVolumeClicked(volumeAdapter.getItem(position));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
