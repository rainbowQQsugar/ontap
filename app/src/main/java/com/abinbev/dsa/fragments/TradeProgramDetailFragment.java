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
import com.abinbev.dsa.adapter.TradeProgramItemAdapterImpl;
import com.abinbev.dsa.model.TradeProgramItem;
import com.abinbev.dsa.ui.presenter.TradeProgramDetailPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TradeProgramDetailFragment extends Fragment implements TradeProgramDetailPresenter.ViewModel {

    private static final String ARGS_MARKET_PROGRAM_ID = "market_program_id";

    @Bind(R.id.trade_program_recycler)
    RecyclerView tradeProgramRecyclerView;

    private TradeProgramItemAdapterImpl adapter;
    private TradeProgramDetailPresenter tradeProgramDetailPresenter;

    public TradeProgramDetailFragment() {
    }

    public static TradeProgramDetailFragment newInstance(String marketProgramId) {
        TradeProgramDetailFragment fragment = new TradeProgramDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_MARKET_PROGRAM_ID, marketProgramId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trade_program, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        tradeProgramRecyclerView.setLayoutManager(layoutManager);

        adapter = new TradeProgramItemAdapterImpl();
        tradeProgramRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        String marketProgramId = args.getString(ARGS_MARKET_PROGRAM_ID);

        if (tradeProgramDetailPresenter == null) {
            tradeProgramDetailPresenter = new TradeProgramDetailPresenter(marketProgramId);
        }
        tradeProgramDetailPresenter.setViewModel(this);
        tradeProgramDetailPresenter.start();
    }

    @Override
    public void onStop() {
        tradeProgramDetailPresenter.stop();
        super.onStop();
    }

    @Override
    public void updateAdapter(final List<TradeProgramItem> tradeProgramItems) {
        adapter.setData(tradeProgramItems);
        adapter.notifyDataSetChanged();
    }
}