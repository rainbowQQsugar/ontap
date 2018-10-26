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
import com.abinbev.dsa.activity.PromotionDetailsActivity;
import com.abinbev.dsa.adapter.PromotionsListAdapter;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.ui.presenter.PromotionsListPresenter;
import com.abinbev.dsa.ui.view.DividerItemDecoration;
import com.abinbev.dsa.ui.view.SortableHeader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by Diana Błaszczyk on 16/10/17.
 */

public class PromotionsTabFragment extends Fragment implements PromotionsListPresenter.ViewModel  {

    private PromotionsListPresenter presenter;
    public PromotionsListAdapter adapter;
    private List<CN_Product_Negotiation__c> promotions = new ArrayList<>();
    private String type;
    private String accountId;

    @Bind(R.id.recycler_view)
    public RecyclerView recyclerView;

    @Nullable
    @Bind({ R.id.promotion_number, R.id.promotion_type, R.id.promotion_description})
    List<SortableHeader> sortableHeaders;

    public static PromotionsTabFragment newInstance(String type, String accountId) {
        PromotionsTabFragment f = new PromotionsTabFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("accountId", accountId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = (String) getArguments().getSerializable("type");
        accountId = (String) getArguments().getSerializable("accountId");

        if (accountId != null){
            if (presenter == null) {
                presenter = new PromotionsListPresenter(accountId, type);
            }
            presenter.setViewModel(this);
            presenter.start();
        }

        adapter = new PromotionsListAdapter();
        adapter.setData(promotions);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.promotions_list_view, container, false);
        ButterKnife.bind(this, root);

        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        adapter.setPromotionClickHandler((promotion, position) -> {
            if (promotion != null) {
                Intent intent = new Intent(getActivity(), PromotionDetailsActivity.class);
                intent.putExtra(PromotionDetailsActivity.PROMOTION_ID, promotion.getId());
                startActivity(intent);
            }
        });

        if (getResources().getBoolean(R.bool.is10InchTablet)) {
            DividerItemDecoration dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
            recyclerView.addItemDecoration(dividerDecoration);
        }

        return root;
    }


    @Override
    public void setData(List<CN_Product_Negotiation__c> promotions) {
        this.promotions.clear();
        this.promotions.addAll(promotions);
        if (adapter != null) {
            adapter.setData(this.promotions);
        }
    }

}
