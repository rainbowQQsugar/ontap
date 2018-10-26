package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeProgressAdapter;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.presenter.KpiDetailsPresenter;

import java.util.HashMap;
import java.util.List;

public class KpiDetailsActivity extends AppBaseActivity implements KpiDetailsPresenter.ViewModel, AdapterView.OnItemClickListener {

    public static final String ARGS_PARENT_KPI_ID = "kpi_id";
    public static final String ARGS_USER_ID = "user_id";
    public static String ARGS_PARENT_CATEGORY_NAME = "category_name";

    public static final String TAG = KpiDetailsActivity.class.getSimpleName();

    @Bind(R.id.kpi_label)
    TextView titleTextView;

    @Bind(R.id.kpi_secondary_label)
    TextView subtitleTextView;

    @Bind(R.id.kpi_list)
    ExpandedGridView gridView;

    @Bind(R.id.buttons_layout)
    LinearLayout buttonsLayout;

    private KpiDetailsPresenter kpiDetailsPresenter;

    private VolumeProgressAdapter volumeAdapter;

    private String volumeId;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.my_360));

        Intent intent = getIntent();
        if (intent != null) {
            volumeId = intent.getStringExtra(ARGS_PARENT_KPI_ID);
            userId = intent.getStringExtra(ARGS_USER_ID);
            final String categoryName = intent.getStringExtra(ARGS_PARENT_CATEGORY_NAME);
            titleTextView.setText(categoryName);
            getSupportActionBar().setTitle(categoryName);

            if (categoryName.equals("Individual KPI")) {
                buttonsLayout.setVisibility(View.VISIBLE);
            }
        }

        gridView.setExpanded(true);
        gridView.setOnItemClickListener(this);
        kpiDetailsPresenter = new KpiDetailsPresenter(this, userId, volumeId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        kpiDetailsPresenter.setViewModel(this);
        kpiDetailsPresenter.start();
    }

    @Override
    protected void onStop() {
        kpiDetailsPresenter.stop();
        super.onStop();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_kpi_details;
    }

    @OnClick(R.id.kpi_button_previous_month)
    public void showKpisForPreviousMonth() {
        kpiDetailsPresenter.getKpisForPreviousMonth();
    }

    @OnClick(R.id.kpi_button_current_month)
    public void showKpisForCurrentMonth() {
        kpiDetailsPresenter.getKpisForCurrentMonth();
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

    @Override /* KpiDetailsPresenter.ViewModel */
    public void setData(List<KPI__c> kpis) {
        if (volumeAdapter == null) {
            volumeAdapter = new VolumeProgressAdapter();
            volumeAdapter.setData(kpis);
            gridView.setAdapter(volumeAdapter);
        }
        else {
            volumeAdapter.setData(kpis);
            volumeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setLastSixMonthData(KpiDetailsPresenter.FetchResult allKpis) {
    }

    @Override /* KpiDetailsPresenter.ViewModel */
    public void setDays(int currentDay, int maxDays) {
        subtitleTextView.setText(getString(R.string.days_of_sale, currentDay, maxDays));
    }

    @Override
    public void setPerformanceKpis(List<CN_KPI_Statistic__c> kpiList) {

    }

    @Override /* KpiDetailsPresenter.ViewModel */
    public void setUser(User user) {
        String noteTitle = getString(R.string.volume) + " " + user.getName();
        getSupportActionBar().setTitle(noteTitle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        kpiDetailsPresenter.onVolumeClicked(volumeAdapter.getItem(position));
    }
}
