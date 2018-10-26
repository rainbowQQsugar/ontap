package com.abinbev.dsa.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.OrderHistoryTabFragment;
import com.abinbev.dsa.fragments.SalesVolumeTabFragment;
import com.abinbev.dsa.model.OrderData;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.SalesVolume;
import com.abinbev.dsa.model.SalesVolumeData;

import java.util.Date;
import java.util.List;

/**
 * Created by Diana Błaszczyk on 27/10/17.
 */

public class OrderHistoryTabsPagerAdapter extends FragmentStatePagerAdapter {

    Context context;

    OrderHistoryTabFragment ordersTab;

    public OrderHistoryTabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        ordersTab = new OrderHistoryTabFragment();
    }


    @Override
    public int getCount() {
        return 1;
    }


    public void setOrdersData(OrderData orders, Date showOrdersSince) {
        ordersTab.setData(orders, showOrdersSince);
    }

    public void setSalesVolumeData(SalesVolumeData salesVolumes, Date showOrdersSince) {
    }

    @Override
    public Fragment getItem(int position) {
        return ordersTab;
    }

    public void setAccountId(String accountId) {
        if (ordersTab != null)
            ordersTab.setAccountId(accountId);
    }
}
