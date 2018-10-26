package com.abinbev.dsa.fragments;

import android.content.Intent;
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
import com.abinbev.dsa.activity.TradeProgramDetailActivity;
import com.abinbev.dsa.adapter.TradeProgramAdapterImpl;
import com.abinbev.dsa.model.TradeProgram;
import com.abinbev.dsa.sync.DefaultSyncBroadcastReceiver;
import com.abinbev.dsa.ui.presenter.TradeProgramPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TradeProgramFragment extends Fragment implements TradeProgramPresenter.ViewModel {

    @Bind(R.id.trade_program_recycler)
    RecyclerView tradeProgramRecyclerView;

    private TradeProgramAdapterImpl adapter;
    private LinearLayoutManager layoutManager;
    private TradeProgramPresenter tradeProgramPresenter;

    public TradeProgramFragment() {
    }

    public static TradeProgramFragment newInstance(String accountId) {
        TradeProgramFragment fragment = new TradeProgramFragment();
        Bundle args = new Bundle();
        args.putString(TradeProgramActivity.ACCOUNT_ID_EXTRA, accountId);
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

        layoutManager = new LinearLayoutManager(getActivity());
        tradeProgramRecyclerView.setLayoutManager(layoutManager);

        adapter = new TradeProgramAdapterImpl() {
            @Override
            public void onBindViewHolder(ViewHolderImpl vh, int position) {
                super.onBindViewHolder(vh, position);
                vh.itemView.setOnClickListener(view -> {
                    TradeProgram tradeProgram = getStored_stored_data().get(position);

                    Intent intent = new Intent(view.getContext(), TradeProgramDetailActivity.class);
                    intent.putExtra(TradeProgramDetailActivity.ARGS_PROGRAM_CONTRACT_ID, tradeProgram.getContractId());
                    intent.putExtra(TradeProgramDetailActivity.ARGS_MARKET_PROGRAM_ID, tradeProgram.getId());
                    view.getContext().startActivity(intent);
                });
            }
        };
        tradeProgramRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        String accountId = args.getString(TradeProgramActivity.ACCOUNT_ID_EXTRA);

        if (tradeProgramPresenter == null) {
            tradeProgramPresenter = new TradeProgramPresenter(accountId, new DefaultSyncBroadcastReceiver());
        }
        tradeProgramPresenter.setViewModel(this);
        tradeProgramPresenter.start();
    }

    @Override
    public void onStop() {
        tradeProgramPresenter.stop();
        super.onStop();
    }

    @Override
    public void updateAdapter(final List<TradeProgram> tradeProgramList) {
        adapter.setData(tradeProgramList);
        adapter.notifyDataSetChanged();
    }
}