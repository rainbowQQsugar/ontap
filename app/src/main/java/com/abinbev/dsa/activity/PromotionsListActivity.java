package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.PromotionsTabsPagerAdapter;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.ui.presenter.PromotionsListPresenter;

import java.util.List;

import butterknife.Bind;

/**
 * Created by lukaszwalukiewicz on 07.01.2016.
 */
public class PromotionsListActivity extends AppBaseDrawerActivity {

    public static final String ARGS_ACCOUNT_ID = "account_id";

    private String accountId;
    PromotionsTabsPagerAdapter tabsPagerAdapter;

    @Bind(R.id.tabs_layout)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("");
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
        }

        tabsPagerAdapter = new PromotionsTabsPagerAdapter(getSupportFragmentManager(), this, accountId);
        viewPager.setAdapter(tabsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_promotions_list_view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
