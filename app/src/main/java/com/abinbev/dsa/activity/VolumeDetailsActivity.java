package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeChildrenAdapter;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.presenter.VolumeDetailsPresenter;
import com.abinbev.dsa.ui.view.VolumeChartView;

import java.util.List;

import butterknife.Bind;

public class VolumeDetailsActivity extends AppBaseActivity implements VolumeDetailsPresenter.ViewModel {

    public static final String ARGS_VOLUME_ID = "volume_id";
    public static final String ARGS_ACCOUNT_ID = "account_id";
    public static final String ARGS_USER_ID = "user_id";

    public static final String TAG = VolumeDetailsActivity.class.getSimpleName();

    @Bind(R.id.volume_name)
    TextView volumeNameTextView;

    @Bind(R.id.volume_details)
    TextView volumeDetailsTextView;

    @Bind(R.id.volume_dates)
    TextView volumeDatesTextView;

    @Bind(R.id.volume_percent)
    TextView volumePercentTextView;

    @Bind(R.id.volume_progress)
    ProgressBar volumeProgress;

    @Bind(R.id.volume_actual_value)
    TextView actualValueTextView;

    @Bind(R.id.volume_actual_units)
    TextView actualUnitsTextView;

    @Bind(R.id.volume_target_value)
    TextView targetValueTextView;

    @Bind(R.id.volume_target_units)
    TextView targetUnitsTextView;

    @Bind(R.id.volume_details_list)
    ExpandedListView detailsListView;

    @Bind(R.id.volume_chart)
    VolumeChartView volumeChart;

    private String volumeId;

    private String accountId;

    private String userId;

    private VolumeChildrenAdapter volumeDetailsAdapter;

    private VolumeDetailsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String noteTitle = getString(R.string.volume);
        getSupportActionBar().setTitle(noteTitle);

        detailsListView.setExpanded(true);
        volumeDetailsAdapter = new VolumeChildrenAdapter();
        detailsListView.setAdapter(volumeDetailsAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            volumeId = intent.getStringExtra(ARGS_VOLUME_ID);
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
            userId = intent.getStringExtra(ARGS_USER_ID);
        }

        if (!TextUtils.isEmpty(accountId)) {
            presenter = VolumeDetailsPresenter.createAccountPresenter(volumeId, accountId);
        }
        else if (!TextUtils.isEmpty(userId)) {
            presenter = VolumeDetailsPresenter.createUserPresenter(volumeId, userId);
        }
        else {
            throw new IllegalStateException("UserId and AccountId cannot be both empty.");
        }

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_details;
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
    protected void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        presenter.stop();
        super.onStop();
    }

    @Override
    public void setKpi(KPI__c kpi) {
        String noteTitle = getString(R.string.volume) + " – " + kpi.getTranslatedCategory();
        getSupportActionBar().setTitle(noteTitle);

        volumeNameTextView.setText(kpi.getTranslatedCategory());
        volumeDetailsTextView.setText(getString(R.string.days_of_sale, kpi.getDaysPassed(),
                kpi.getTotalDays()));

        double actual = kpi.getActual();
        double target = kpi.getTarget();
        int percentProgress = target == 0 ? 0 : (int) ((actual / target) * 100);

        volumeDatesTextView.setText(String.format("%s / %s", kpi.getStartDate(), kpi.getEndDate()));
        volumePercentTextView.setText(percentProgress + "%");
        volumeProgress.setProgress(percentProgress);
        actualValueTextView.setText(Integer.toString((int) actual));
        actualUnitsTextView.setText(kpi.getTranslatedUnit());
        targetValueTextView.setText(Integer.toString((int) target));
        targetUnitsTextView.setText(kpi.getTranslatedUnit());
    }

    @Override
    public void setHistoricalKpis(List<KPI__c> history) {
        volumeChart.setKpis(history);
    }


    @Override
    public void setChildrenKpis(List<KPI__c> detailKpis) {
        volumeDetailsAdapter.setItems(detailKpis);
        volumeDetailsAdapter.notifyDataSetChanged();
    }

    @Override
    public void setAccountName(String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }
}
