package com.abinbev.dsa.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.CoverageDetailsFragment;

public class CoverageDetailsActivity extends AppBaseActivity implements CoverageDetailsFragment.ViewModel {

    public static final String TAG = CoverageDetailsActivity.class.getSimpleName();

    public static final String ARGS_COVERAGE_ID = "coverage_id";

    public static final String ARGS_ACCOUNT_ID = "account_id";

    private static final String FRAGMENT_COVERAGE = "fragment_coverage";

    String coverageId;

    String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String noteTitle = getString(R.string.coverage);
        getSupportActionBar().setTitle(noteTitle);

        Intent intent = getIntent();
        if (intent != null) {
            coverageId = intent.getStringExtra(ARGS_COVERAGE_ID);
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
        }

        FragmentManager fm = getFragmentManager();
        CoverageDetailsFragment coverageFragment =
                (CoverageDetailsFragment) fm.findFragmentByTag(FRAGMENT_COVERAGE);

        if (coverageFragment == null) {
            coverageFragment = CoverageDetailsFragment.newInstance(coverageId, accountId);

            fm.beginTransaction()
                    .add(R.id.fragment_container, coverageFragment, FRAGMENT_COVERAGE)
                    .commit();
        }

        coverageFragment.setFragmentViewModel(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_coverage_details;
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

    @Override // CoverageDetailsFragment.ViewModel
    public void setHeaderTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override // CoverageDetailsFragment.ViewModel
    public void setHeaderSubtitle(String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }
}
