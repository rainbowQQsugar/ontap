package com.abinbev.dsa.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.PromotionsTabFragment;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.ui.presenter.PromotionsListPresenter;

import java.util.List;

/**
 * Created by Diana Błaszczyk on 16/10/17.
 */

public class PromotionsTabsPagerAdapter extends FragmentStatePagerAdapter {

    public static final String POC_PROMOTION_TYPE = "POC Promotion";
    public static final String CONSUMER_PROMOTION_TYPE = "Consumer Promotion";

    PromotionsTabFragment[] tabs;
    Context context;

    public PromotionsTabsPagerAdapter(FragmentManager fm, Context context, String accountId) {
        super(fm);
        this.context = context;
        tabs = new PromotionsTabFragment[2];
        tabs[0] = PromotionsTabFragment.newInstance(POC_PROMOTION_TYPE, accountId);
        tabs[1] = PromotionsTabFragment.newInstance(CONSUMER_PROMOTION_TYPE, accountId);
    }

    @Override
    public Fragment getItem(int position) {
        return tabs[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (position == 0 ? context.getResources().getString(R.string.poc_promotions) :
        context.getResources().getString(R.string.consumer_promotions));
    }

}
