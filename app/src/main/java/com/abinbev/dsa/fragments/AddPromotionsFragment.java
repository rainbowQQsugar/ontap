package com.abinbev.dsa.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AddPromotionAdapter;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Promociones__c;
import com.abinbev.dsa.ui.presenter.PromotionsListPresenter;
import com.abinbev.dsa.ui.view.negotiation.NegotiationHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 2/1/16.
 */
public class AddPromotionsFragment extends Fragment implements PromotionsListPresenter.ViewModel {

    private static final String ARGS_ACCOUNT_ID = "args_account_id";
    private static final String TAG = AddPromotionsFragment.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.promotions_list)
    RecyclerView promotionsList;

    private AddPromotionAdapter adapter;
    private PromotionsListPresenter presenter;
    private String accountId;
    private NegotiationHelper negotiationHelper;

    public AddPromotionsFragment() {
        super();
    }

    public static AddPromotionsFragment newInstance(String accountId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ACCOUNT_ID, accountId);

        AddPromotionsFragment fragment = new AddPromotionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.accountId = getArguments().getString(ARGS_ACCOUNT_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.negotiations);
        getSupportActionBar().setSubtitle(R.string.additional_promo_codes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        promotionsList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            negotiationHelper = (NegotiationHelper) getActivity();
        } catch (Exception e) {
            Log.e(TAG, "onAttach: ", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter == null) {
            presenter = new PromotionsListPresenter(accountId, "");
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void setData(List<CN_Product_Negotiation__c> promotions) {
     //   adapter = new AddPromotionAdapter(promotions); //todo
    //    promotionsList.setAdapter(adapter);
    }


    @OnClick(R.id.add_btn)
    public void addItems() {
        if (negotiationHelper != null && adapter != null) {
            negotiationHelper.addPromoCodes(adapter.getCodes());
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

}
