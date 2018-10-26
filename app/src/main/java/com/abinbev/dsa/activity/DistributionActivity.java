package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.ui.presenter.DistributionPresenter;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;

import butterknife.OnClick;

public class DistributionActivity extends DynamicViewActivity implements DistributionPresenter.ViewModel {

//    private static final String TAG = "DistributionActivity";

    public static final String ARGS_DISTRIBUTION_ID = "distribution_id";
    public static final String ARGS_DISTRIBUTION_NAME = "distribution_name";

    private DistributionPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String distributionId = intent.getStringExtra(ARGS_DISTRIBUTION_ID);
        String distributionName = intent.getStringExtra(ARGS_DISTRIBUTION_NAME);

        getSupportActionBar().setTitle(distributionName);
        getSupportActionBar().setSubtitle(R.string.distribution_list_title);

        presenter = new DistributionPresenter(distributionId);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_distribution;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override /* DistributionPresenter.ViewModel */
    public void setDistribution(Distribution distribution) {
        buildLayout(AbInBevObjects.CN_DISTRIBUTION, distribution);
    }

    @Override /* DistributionPresenter.ViewModel */
    public void close() {
        finish();
    }

    @OnClick(R.id.delete_button)
    void onSaveClicked() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.distribution_are_you_sure_you_want_to_delete)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> presenter.deleteDistribution())
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
}
