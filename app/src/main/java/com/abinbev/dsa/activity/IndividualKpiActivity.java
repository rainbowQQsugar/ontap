package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.IndividualKpiAdapter;
import com.abinbev.dsa.fragments.UserKpiDetailsFragment;

import butterknife.Bind;

public class IndividualKpiActivity extends AppBaseDrawerActivity {

    @Bind(R.id.tabs_layout)
    TabLayout tabsLayout;
    @Bind(R.id.vp_kpi)
    ViewPager vpKpi;
    public static String BACIS_KPI = "bacis_kpi";
    private String TAG = getClass().getSimpleName();
    private IndividualKpiAdapter individualKpiAdapter;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_individual_kpi;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.individual_kpi));
        String kpiId = getIntent().getStringExtra(IndividualKpiActivity.BACIS_KPI);
        String userId = getIntent().getStringExtra(KpiDetailsActivity.ARGS_USER_ID);
        String categoryName = getIntent().getStringExtra(UserKpiDetailsFragment.ARGS_PARENT_CATEGORY_NAME);
        tabsLayout.setupWithViewPager(vpKpi);
        individualKpiAdapter = new IndividualKpiAdapter(getSupportFragmentManager(), this, kpiId, userId, categoryName);
        vpKpi.setAdapter(individualKpiAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onSyncCompleted() {
        if (individualKpiAdapter != null) individualKpiAdapter.onSyncCompleted();
    }
}
