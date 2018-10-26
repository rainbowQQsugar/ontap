package com.abinbev.dsa.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeProgressAdapter;
import com.abinbev.dsa.fragments.UserKpiDetailsFragment;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.sync.DynamicFetchBroadcastReceiver;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.presenter.MainScreenPresenter;
import com.abinbev.dsa.ui.presenter.MorningMeetingPresenter;
import com.abinbev.dsa.ui.presenter.MorningMeetingPresenter.ViewState;
import com.abinbev.dsa.ui.presenter.UserDetailsPresenter;
import com.abinbev.dsa.ui.view.CheckInButton;
import com.abinbev.dsa.ui.view.KpiProgressView;
import com.abinbev.dsa.ui.view.UserPerformanceIndicatorsView;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.dsa.location.LocationHandlerProvider;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class UserDetailsActivity extends AppBaseDrawerActivity implements UserDetailsPresenter.ViewModel,
        MorningMeetingPresenter.ViewModel, CheckInButton.OnCheckButtonClickedListener,
        MainScreenPresenter.ViewModel, AdapterView.OnItemClickListener {

    private static final String TAG = UserDetailsActivity.class.getSimpleName();

    private static final int REQUEST_CODE_TAKE_PICTURE = 178;

    public static final String ARG_SHOW_QUEUE_DIALOG = "arg_show_queue_dialog";
    public static final String ARG_IS_OPENED_FROM_DRAWER = "arg_is_opened_from_drawer";

    @Bind(R.id.user_name)
    TextView userNameTextView;

    @Bind(R.id.user_sale_days_count)
    TextView saleDaysCount;

    @Bind(R.id.performance_indicators_view)
    UserPerformanceIndicatorsView performanceView;

    @Bind(R.id.user_progress_container)
    LinearLayout progressContainer;

    @Bind(R.id.check_in_button)
    CheckInButton checkInButton;

    @Bind(R.id.user_progress_grid)
    ExpandedGridView userProgressGrid;
    @Bind(R.id.view_all)
    TextView viewAll;
    @Bind(R.id.up_to_now)
    TextView upToNow;

    private Handler handler = new Handler();

    private VolumeProgressAdapter volumeAdapter;

    private UserDetailsPresenter userDetailsPresenter;

    private MorningMeetingPresenter morningMeetingPresenter;

    private MainScreenPresenter mainScreenPresenter;

    private DynamicFetchBroadcastReceiver dynamicFetchBroadcastReceiver;

    private boolean isFirstStart;

    private AlertDialog dynamicFetchError;
    private List<List<KPI__c>> kpis;
    private User user;
    private List<CN_KPI_Statistic__c> performanceKpis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showQueueDialog = getIntent().getBooleanExtra(ARG_SHOW_QUEUE_DIALOG, false);
        isFirstStart = savedInstanceState == null
                && !getIntent().getBooleanExtra(ARG_IS_OPENED_FROM_DRAWER, false); // Opening screen from drawer is not treated as first start.

        checkInButton.setCheckInValues(R.string.morning_meeting_checkin, R.string.please_check_into_morning_meeting);
        checkInButton.setCheckOutValues(R.string.morning_meeting_checkout, R.string.please_check_out_from_morning_meeting);
        checkInButton.setOnCheckButtonClickedListener(this);

        userDetailsPresenter = new UserDetailsPresenter(this, isFirstStart);
        userDetailsPresenter.setViewModel(this);

        dynamicFetchBroadcastReceiver = new DynamicFetchBroadcastReceiver();
        morningMeetingPresenter = new MorningMeetingPresenter(LocationHandlerProvider.createLocationHandler(this), dynamicFetchBroadcastReceiver);
        morningMeetingPresenter.onLoadInstanceState(savedInstanceState);
        morningMeetingPresenter.setViewModel(this);

        mainScreenPresenter = new MainScreenPresenter(showQueueDialog);
        mainScreenPresenter.setViewModel(this);

        volumeAdapter = new VolumeProgressAdapter();
        userProgressGrid.setAdapter(volumeAdapter);
        userProgressGrid.setExpanded(true);
        userProgressGrid.setVisibility(View.GONE);
        userProgressGrid.setOnItemClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (isFirstStart && intent.getBooleanExtra(ARG_IS_OPENED_FROM_DRAWER, false)) {
            isFirstStart = false;
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_details;
    }

    @Override
    public void onRefresh() {
    }

    @Override
    protected void onSyncCompleted() {
        super.onSyncCompleted();
        userDetailsPresenter.start();
        morningMeetingPresenter.start();
        mainScreenPresenter.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        userDetailsPresenter.start();
        morningMeetingPresenter.start();
        mainScreenPresenter.start();
    }

    @Override
    protected void onStop() {
        userDetailsPresenter.stop();
        morningMeetingPresenter.stop();
        mainScreenPresenter.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        userDetailsPresenter.onDestroy();
        morningMeetingPresenter.onDestroy();
        mainScreenPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        morningMeetingPresenter.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri pictureUri = data.getParcelableExtra(CheckInWithPictureActivity.RESULT_PICTURE_URI);
                String description = data.getStringExtra(CheckInWithPictureActivity.RESULT_DESCRIPTION);
                morningMeetingPresenter.onPictureTaken(pictureUri, description);
            }
        }
    }

    private void setUser(User user) {
        getSupportActionBar().setTitle(user.getName());
        userNameTextView.setText(user.getName());
        performanceView.setUserId(user.getId());
    }

    private void setKpisViews(UserDetailsPresenter.State state) {
        userProgressGrid.setVisibility(View.GONE);
        progressContainer.removeAllViews();
        progressContainer.setVisibility(View.VISIBLE);
        this.user = state.user;
        this.kpis = state.kpis;
        if (state.performanceKpis == null || state.performanceKpis.isEmpty()) return;
        LinearLayout layout = new LinearLayout(getContext());
        CN_KPI_Statistic__c cn_kpi_statistic__c = state.performanceKpis.get(0);
        if (cn_kpi_statistic__c == null) return;
        upToNow.setText(getString(R.string.up_to_now) + " " + cn_kpi_statistic__c.geteDateValue());
        List<KpiProgressView> kpiProgressView = createKpiProgressView(cn_kpi_statistic__c);
        for (KpiProgressView progressView : kpiProgressView) {
            layout.addView(progressView);
        }
        progressContainer.addView(layout);
    }

    private void setLocationProgressVisible(boolean isVisible) {
        if (isVisible) {
            checkInButton.showValidationError(R.string.please_wait_for_location);
        } else {
            checkInButton.hideValidationError();
        }
    }

    @NonNull
    private List<KpiProgressView> createKpiProgressView(CN_KPI_Statistic__c performanceKpis) {

        List<KpiProgressView> kpiProgressViews = new ArrayList<>();
        KpiProgressView kpiProgressView = new KpiProgressView(getContext());
        kpiProgressView.setUpWeeklyVisitProgress(performanceKpis);
        KpiProgressView kpiProgressView1 = new KpiProgressView(getContext());
        kpiProgressView1.setUpMonthlyVisitProgress2(performanceKpis);
        kpiProgressViews.add(kpiProgressView);
        kpiProgressViews.add(kpiProgressView1);
        return kpiProgressViews;
    }

    @Override /* UserDetailsPresenter.ViewModel */
    public void setState(UserDetailsPresenter.State state) {
        setUser(state.user);
        setKpisViews(state);

        if (state.showTimeZoneWarning) {
            showTimeZoneWarning(state.localTimeZone, state.remoteTimeZone);
        }
    }

    private void showTimeZoneWarning(String localTimeZone, String remoteTimeZone) {
        if (!isFirstStart) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(getString(R.string.time_zone_warning, remoteTimeZone, localTimeZone))
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

    @Override /* MorningMeetingPresenter.ViewModel */
    public void setViewState(ViewState viewState) {
        if (viewState instanceof ViewState.MorningMeetingCheckIn) {
            ViewState.MorningMeetingCheckIn checkInViewState = (ViewState.MorningMeetingCheckIn) viewState;

            checkInButton.setVisibility(View.VISIBLE);
            checkInButton.setState(CheckInButton.State.CHECK_IN);
            setLocationProgressVisible(checkInViewState.showLocationProgress);
        } else if (viewState instanceof ViewState.MorningMeetingCheckOut) {
            ViewState.MorningMeetingCheckOut checkOutViewState = (ViewState.MorningMeetingCheckOut) viewState;

            checkInButton.setVisibility(View.VISIBLE);
            checkInButton.setState(CheckInButton.State.CHECK_OUT);
            setLocationProgressVisible(checkOutViewState.showLocationProgress);
        } else if (viewState instanceof ViewState.Basic) {
            checkInButton.setVisibility(View.GONE);
        } else {
            Log.w(TAG, "Unknown state " + viewState);
        }
    }

    @Override /* MorningMeetingPresenter.ViewModel */
    public void setSyncStatus(SyncStatus syncStatus) {
        if (dynamicFetchError != null && dynamicFetchError.isShowing()) {
            dynamicFetchError.dismiss();
            dynamicFetchError = null;
        }

        setSimpleSyncProgress(syncStatus);
    }

    @Override /* MorningMeetingPresenter.ViewModel */
    public void promptForPicture(int messageId, boolean isCommentRequired) {
        startActivityForResult(
                new Intent(this, CheckInWithPictureActivity.class)
                        .putExtra(CheckInWithPictureActivity.ARGS_MESSAGE, getString(messageId))
                        .putExtra(CheckInWithPictureActivity.ARGS_IS_COMMENT_REQUIRED, isCommentRequired),
                REQUEST_CODE_TAKE_PICTURE);
    }

    @Override /* MorningMeetingPresenter.ViewModel */
    public void showDynamicFetchError(String errorMessage) {
        dynamicFetchError = new AlertDialog.Builder(getContext())
                .setTitle(R.string.sync_error)
                .setMessage(errorMessage)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.try_again,
                        (dialog, which) -> morningMeetingPresenter.retryDynamicFetch())
                .show();
    }

    @Override /* MorningMeetingPresenter.ViewModel */
    public void doDeltaSync() {
        // start delta sync
        long delay = 300;
        handler.postDelayed(() -> {
            if (!isDestroyed()) {
                syncProgressView.startSync(false, false);
            }
        }, delay);
    }

    @Override /* MorningMeetingPresenter.ViewModel */
    public Activity getActivity() {
        return this;
    }

    @Override /* CheckInButton.OnCheckButtonClickedListener */
    public void onCheckInClicked() {
        morningMeetingPresenter.onCheckInClicked();
    }

    @Override /* CheckInButton.OnCheckButtonClickedListener */
    public void onCheckOutClicked() {
        morningMeetingPresenter.onCheckOutClicked();
    }

    @Override /* MainScreenPresenter.ViewModel */
    public Context getContext() {
        return this;
    }

    @Override /* MainScreenPresenter.ViewModel */
    public void promptQueueDataUpload() {
        startActivity(new Intent(this, InitialQueueActionDialogActivity.class));
    }

    @Override /* MainScreenPresenter.ViewModel */
    public void promptManifestUpdate() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.full_sync)
                .setMessage(R.string.manifest_update_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> syncProgressView.startSync(true, true))
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .show();
    }

    @Nullable
    @OnItemClick(R.id.volume_list)
    public void onChildVolumeClicked(int position) {
    }

    @Override
    public void onNewLocationReceived(Location location) {
        if (morningMeetingPresenter != null)
            morningMeetingPresenter.onNewLocationReceived(location);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @OnClick(R.id.view_all)
    public void onViewClicked() {
        Intent intent = new Intent(this, IndividualKpiActivity.class);
        if (kpis != null && !kpis.isEmpty()) {
            intent.putExtra(IndividualKpiActivity.BACIS_KPI, kpis.get(0).get(0).getKpiNum());
            intent.putExtra(KpiDetailsActivity.ARGS_USER_ID, user.getId());
            intent.putExtra(UserKpiDetailsFragment.ARGS_PARENT_CATEGORY_NAME, kpis.get(0).get(0).getCategory());
        }
        startActivity(intent);
    }
}
