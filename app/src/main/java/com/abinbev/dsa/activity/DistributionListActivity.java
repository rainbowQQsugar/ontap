package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.DistributionListFragment;

import butterknife.OnClick;

/**
 * Created by Adam Chodera on 8.08.2017.
 */

public class DistributionListActivity extends AppBaseDrawerActivity {

    public static final String ACCOUNT_ID_EXTRA = "accountId";

    private static final int REQUEST_CODE_CREATE_DISTRIBUTION = 10;

    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);

        final DistributionListFragment sprListFragment = DistributionListFragment.newInstance(accountId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, sprListFragment)
                .commit();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_distribution_list;
    }

    @OnClick(R.id.add_product_to_distribution_list)
    public void addProductToDistributionList() {
        Intent intent = new Intent(this, SelectProductForDistributionListActivity.class);
        intent.putExtra(SelectProductForDistributionListActivity.EXTRA_ACCOUNT_ID, accountId);
        startActivityForResult(intent, REQUEST_CODE_CREATE_DISTRIBUTION);
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
}