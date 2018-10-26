package com.abinbev.dsa.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.Account360DetailsFragment;
import com.abinbev.dsa.fragments.CoverageDetailsFragment;
import com.abinbev.dsa.fragments.VolumeDetailsFragment;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.DynamicFetchControls;
import com.abinbev.dsa.ui.view.Account360Header;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;

import butterknife.Bind;

public class AccountOverviewActivity extends AppBaseDrawerActivity implements
        Account360Header.AccountDetailsCallback, Account360DetailsFragment.OnCoverageClickedListener,
        Account360DetailsFragment.OnVolumeClickedListener,
        CoverageDetailsFragment.OnCoverageCloseClickedListener, CoverageDetailsFragment.ViewModel,
        FragmentManager.OnBackStackChangedListener, VolumeDetailsFragment.ViewModel,
        VolumeDetailsFragment.OnVolumeDetailsCloseClickedListener {

    public static final String ACCOUNT_ID_EXTRA = "account_id";
    public static final String EVENT_ID_EXTRA = "event_id";
    public static final String IS_CHECKOUT_PROMPT_EXTRA = "is_checkout_prompt";

    public static final String RESULT_IS_CHECKED_IN = "isCheckedIn";

    private static final String FRAGMENT_ACCOUNT_DETAILS = "fragment_details";
    private static final String FRAGMENT_COVERAGE = "fragment_coverage";
    private static final String FRAGMENT_VOLUME = "fragment_volume";

    @Bind(R.id.account_header)
    Account360Header account360Header;

    @Nullable
    @Bind(R.id.details_toolbar)
    Toolbar detailsToolbar;

    private AlertDialog dynamicFetchError;

    public String accountId;

    public String eventId;

    public boolean isCheckedIn;

    boolean isCheckoutPrompt;

    Menu toolbarMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
        eventId = getIntent().getStringExtra(EVENT_ID_EXTRA);
        isCheckoutPrompt = getIntent().getBooleanExtra(IS_CHECKOUT_PROMPT_EXTRA, false);

        setSyncListenerForChecking(account360Header);
        account360Header.setInitialData(accountId, eventId, isCheckoutPrompt);
        account360Header.setAccountDetailsCallback(this);

        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);

        // Setup account details fragment.
        Account360DetailsFragment detailsFragment = (Account360DetailsFragment) fm.findFragmentByTag(FRAGMENT_ACCOUNT_DETAILS);
        if (detailsFragment == null) {
            detailsFragment = Account360DetailsFragment.newInstance(accountId);
            fm.beginTransaction()
                    .add(R.id.fragment_container, detailsFragment, FRAGMENT_ACCOUNT_DETAILS)
                    .commit();
        }
        detailsFragment.setOnCoverageClickedListener(this);
        detailsFragment.setOnVolumeClickedListener(this);

        // Make sure that if Coverage fragment exists it has actual listeners.
        CoverageDetailsFragment coverageDetailsFragment = (CoverageDetailsFragment) fm.findFragmentByTag(FRAGMENT_COVERAGE);
        if (coverageDetailsFragment != null) {
            coverageDetailsFragment.setOnCloseClickedListener(this);
            coverageDetailsFragment.setFragmentViewModel(this);
        }

        // Make sure that if Volume fragment exists it has actual listeners.
        VolumeDetailsFragment volumeDetailsFragment = (VolumeDetailsFragment) fm.findFragmentByTag(FRAGMENT_VOLUME);
        if (volumeDetailsFragment != null) {
            volumeDetailsFragment.setFragmentViewModel(this);
            volumeDetailsFragment.setOnCloseClickedListener(this);
        }

        onBackStackChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Account360Header.REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                account360Header.checkInWithPictureSuccess(data);
            }
            else {
                account360Header.checkInWithPictureCancelled();
            }
        }
        else if (requestCode == AttachmentUtils.SELECT_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = AttachmentUtils.fileUri != null ? AttachmentUtils.fileUri : data.getData();

                if (uri == null) {
                    Log.e("Babu", "data is null!");
                    return;
                }

                Intent intent = AttachmentUploadService.uploadAccountPhoto(this, uri, accountId);
                startService(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        getFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        toolbarMenu = menu;

        MenuItem item = menu.findItem(R.id.sync);
        if (item != null) {
            item.setVisible(isShowingMainFragment());
        }

        return result;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_account_overview;
    }

    @Override
    protected boolean attachToRoot() {
        return true;
    }

    @Override
    public void onRefresh() {
        checkAccount(accountId);
    }

    @Override
    protected void onSyncCompleted() {
        super.onSyncCompleted();
        account360Header.setInitialData(accountId, eventId, isCheckoutPrompt);
        account360Header.setAccountDetailsCallback(this);


        Fragment fragment = getFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment instanceof Account360DetailsFragment) {
            ((Account360DetailsFragment) fragment).onRefresh();
        }
    }

    @Override
    public void onCheckInChanged(boolean isCheckedIn) {
        setResult(RESULT_OK, new Intent()
            .putExtra(RESULT_IS_CHECKED_IN, isCheckedIn));
        this.isCheckedIn = isCheckedIn;
    }

    @Override
    public void setSyncStatus(SyncStatus syncStatus) {
        if (dynamicFetchError != null && dynamicFetchError.isShowing()) {
            dynamicFetchError.dismiss();
            dynamicFetchError = null;
        }

        setSimpleSyncProgress(syncStatus);
    }

    @Override
    public void showDynamicFetchError(String errorMessage, DynamicFetchControls dynamicFetchControls) {
        dynamicFetchError = new AlertDialog.Builder(this)
                .setTitle(R.string.sync_error)
                .setMessage(errorMessage)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.try_again,
                        (dialog, which) -> dynamicFetchControls.retryDynamicFetch())
                .show();
    }

    @Override // Account360DetailsFragment.OnCoverageClickedListener
    public void onCoverageClicked(KPI__c kpi) {
        openCoverageDetails(kpi);
    }

    @Override // Account360DetailsFragment.OnVolumeClickedListener
    public void onVolumeClicked(KPI__c kpi) {
        openVolumeDetails(kpi);
    }

    @Override // CoverageDetailsFragment.OnCoverageCloseClickedListener
    public void onCoverageCloseClicked() {
        getFragmentManager().popBackStack();
    }

    @Override // FragmentManager.OnBackStackChangedListener
    public void onBackStackChanged() {
        if (!getResources().getBoolean(R.bool.is10InchTablet)) return;

        if (isShowingMainFragment()) {
            toolbar.setVisibility(View.VISIBLE);
            detailsToolbar.setVisibility(View.GONE);

            setupDefaultToolbar();

            if (toolbarMenu != null) {
                toolbarMenu.findItem(R.id.sync).setVisible(true);
            }
        }
        else {
            toolbar.setVisibility(View.GONE);
            detailsToolbar.setVisibility(View.VISIBLE);

            setSupportActionBar(detailsToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setSubtitle(null);

            if (toolbarMenu != null) {
                toolbarMenu.findItem(R.id.sync).setVisible(true);
            }
        }
    }

    @Override // CoverageDetailsFragment.ViewModel and VolumeDetailsFragment.ViewModel
    public void setHeaderTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override // CoverageDetailsFragment.ViewModel and VolumeDetailsFragment.ViewModel
    public void setHeaderSubtitle(String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

    @Override // VolumeDetailsFragment.OnVolumeDetailsCloseClickedListener
    public void onVolumeDetailsCloseClicked() {
        getFragmentManager().popBackStack();
    }

    private void openCoverageDetails(KPI__c kpi) {
        if (getResources().getBoolean(R.bool.is10InchTablet)) {
            FragmentManager fm = getFragmentManager();
            CoverageDetailsFragment coverageFragment =
                    (CoverageDetailsFragment) fm.findFragmentByTag(FRAGMENT_COVERAGE);

            if (coverageFragment == null) {
                coverageFragment = CoverageDetailsFragment.newInstance(kpi.getId(), accountId);

                fm.beginTransaction()
                        .replace(R.id.fragment_container, coverageFragment, FRAGMENT_COVERAGE)
                        .addToBackStack(null)
                        .commit();
            }
            coverageFragment.setShowCloseButton(true);
            coverageFragment.setCoverageListExpanded(true);
            coverageFragment.setOnCloseClickedListener(this);
            coverageFragment.setFragmentViewModel(this);
        }
        else {
            startActivity(new Intent(this, CoverageDetailsActivity.class)
                    .putExtra(CoverageDetailsActivity.ARGS_ACCOUNT_ID, accountId)
                    .putExtra(CoverageDetailsActivity.ARGS_COVERAGE_ID, kpi.getId()));
        }
    }

    private void openVolumeDetails(KPI__c kpi) {
        if (getResources().getBoolean(R.bool.is10InchTablet)) {
            FragmentManager fm = getFragmentManager();
            VolumeDetailsFragment volumeFragment =
                    (VolumeDetailsFragment) fm.findFragmentByTag(FRAGMENT_VOLUME);

            if (volumeFragment == null) {
                volumeFragment = VolumeDetailsFragment.newAccountVolumeInstance(accountId, null, kpi.getId());

                fm.beginTransaction()
                        .replace(R.id.fragment_container, volumeFragment, FRAGMENT_VOLUME)
                        .addToBackStack(null)
                        .commit();
            }

            volumeFragment.setFragmentViewModel(this);
            volumeFragment.setOnCloseClickedListener(this);
        }
        else {
            startActivity(new Intent(this, VolumeDetailsActivity.class)
                    .putExtra(VolumeDetailsActivity.ARGS_VOLUME_ID, kpi.getId())
                    .putExtra(VolumeDetailsActivity.ARGS_ACCOUNT_ID, accountId));
        }
    }

    private boolean isShowingMainFragment() {
        Fragment topFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        return topFragment instanceof Account360DetailsFragment;
    }

}
