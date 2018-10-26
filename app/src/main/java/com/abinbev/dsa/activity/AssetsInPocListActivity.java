package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AssetInPocListAdapter;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.ui.presenter.AssetsInPocPresenter;

import java.util.List;

import butterknife.Bind;

public class AssetsInPocListActivity extends AppBaseDrawerActivity implements AssetsInPocPresenter.ViewModel {

    public static final String TAG = AssetsInPocListActivity.class.getSimpleName();
    public static final String ACCOUNT_ID_EXTRA = "account_id";

    public static final String ARGS_ACCOUNT_ID = "account_id";

    private AssetInPocListAdapter adapter;
    private AssetsInPocPresenter assetsInPocPresenter;
    private String accountId;

    @Bind(R.id.assets_list)
    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AssetInPocListAdapter(this);
        listView.setAdapter(adapter);
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
        }
        getSupportActionBar().setTitle("");
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_assets_in_poc_list_view;
    }

    @Override
    protected void onPause() {
        super.onPause();
        assetsInPocPresenter.stop();
    }

    @Override
    public void setAssets(List<Account_Asset__c> assets, String recordName) {
        adapter.setData(assets, recordName);
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
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
            if (assetsInPocPresenter == null) {
                assetsInPocPresenter = new AssetsInPocPresenter(accountId);
            }
            assetsInPocPresenter.setViewModel(this);
            assetsInPocPresenter.start();
        }
    }
}
