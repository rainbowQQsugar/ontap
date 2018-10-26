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
import com.abinbev.dsa.activity.SprListActivity;
import com.abinbev.dsa.adapter.SprAdapter;
import com.abinbev.dsa.model.CN_SPR__c;
import com.abinbev.dsa.sync.DefaultSyncBroadcastReceiver;
import com.abinbev.dsa.ui.presenter.SprListPresenter;
import com.abinbev.dsa.ui.view.DividerItemDecoration;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Adam Chodera on 7.07.2017.
 */

public class SprListFragment extends Fragment implements SprListPresenter.ViewModel {

    @Bind(R.id.spr_recycler)
    RecyclerView sprRecyclerView;

    private SprAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SprListPresenter sprListPresenter;

    public SprListFragment() {
    }

    public static SprListFragment newInstance(String accountId) {
        SprListFragment fragment = new SprListFragment();
        Bundle args = new Bundle();
        args.putString(SprListActivity.ACCOUNT_ID_EXTRA, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spr_list, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layoutManager = new LinearLayoutManager(getActivity());
        sprRecyclerView.setLayoutManager(layoutManager);

        adapter = new SprAdapter.ViewTyped();
        sprRecyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        sprRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        String accountId = args.getString(SprListActivity.ACCOUNT_ID_EXTRA);

        if (sprListPresenter == null) {
            sprListPresenter = new SprListPresenter(accountId, new DefaultSyncBroadcastReceiver());
        }
        sprListPresenter.start(this);
    }

    @Override
    public void onStop() {
        sprListPresenter.stop(this);
        super.onStop();
    }

    @Override
    public void updateAdapter(final List<CN_SPR__c> sprData) {
        adapter.setData(sprData);
        adapter.notifyDataSetChanged();
    }
}