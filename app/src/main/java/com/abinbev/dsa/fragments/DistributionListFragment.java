package com.abinbev.dsa.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.TradeProgramActivity;
import com.abinbev.dsa.adapter.DistributionAdapter;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.sync.DefaultSyncBroadcastReceiver;
import com.abinbev.dsa.ui.presenter.DistributionListPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DistributionListFragment extends Fragment implements DistributionListPresenter.ViewModel {

    @Bind(R.id.distribution_recycler)
    RecyclerView distributionRecyclerView;

    private DistributionAdapter adapter;
    private LinearLayoutManager layoutManager;
    private DistributionListPresenter distributionListPresenter;

    public DistributionListFragment() {
    }

    public static DistributionListFragment newInstance(String accountId) {
        DistributionListFragment fragment = new DistributionListFragment();
        Bundle args = new Bundle();
        args.putString(TradeProgramActivity.ACCOUNT_ID_EXTRA, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_distribution, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layoutManager = new LinearLayoutManager(getActivity());
        distributionRecyclerView.setLayoutManager(layoutManager);

        adapter = new DistributionAdapter();
        distributionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        String accountId = args.getString(TradeProgramActivity.ACCOUNT_ID_EXTRA);

        if (distributionListPresenter == null) {
            distributionListPresenter = new DistributionListPresenter(accountId, new DefaultSyncBroadcastReceiver());
        }
        distributionListPresenter.start(this);
    }

    @Override
    public void onStop() {
        distributionListPresenter.stop();
        super.onStop();
    }

    @Override
    public void updateAdapter(final List<Distribution> distribution) {
        adapter.setData(distribution);
        adapter.notifyDataSetChanged();
    }
}