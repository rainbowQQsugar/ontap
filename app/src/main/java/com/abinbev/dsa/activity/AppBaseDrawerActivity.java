package com.abinbev.dsa.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.BuildConfig;
import com.abinbev.dsa.R;
import com.abinbev.dsa.di.component.AppComponent;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.CN_App_Version__c;
import com.abinbev.dsa.model.CN_Notification_Message__c;
import com.abinbev.dsa.ui.presenter.CheckInPresenter;
import com.abinbev.dsa.ui.presenter.CountPresenter;
import com.abinbev.dsa.ui.view.AppVersionUpgrade;
import com.abinbev.dsa.ui.view.SimpleSyncProgressView;
import com.abinbev.dsa.ui.view.SyncProgressView;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DataUtils;
import com.abinbev.dsa.utils.DownLoadUtils;
import com.abinbev.dsa.utils.PermissionManager;
import com.abinbev.dsa.utils.ViewUtils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncEngine;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus.SyncStatusState;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;
import com.salesforce.dsa.app.sync.ClearAllTempTemporaryFilesTask;
import com.salesforce.dsa.app.ui.activity.DigitalSalesAid;
import com.salesforce.dsa.app.ui.dialogs.DownDialog;
import com.salesforce.dsa.location.LocationReceiver;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import static com.abinbev.dsa.utils.PermissionManager.ACCOUNTS;
import static com.abinbev.dsa.utils.PermissionManager.CALCULATOR;
import static com.abinbev.dsa.utils.PermissionManager.CHATTER;
import static com.abinbev.dsa.utils.PermissionManager.DSA;
import static com.abinbev.dsa.utils.PermissionManager.FULL_SYNC;
import static com.abinbev.dsa.utils.PermissionManager.PROSPECT_LIST;
import static com.abinbev.dsa.utils.PermissionManager.QUIZZES;
import static com.abinbev.dsa.utils.PermissionManager.USER_360;
import static com.abinbev.dsa.utils.PermissionManager.VISIT_LIST;

/**
 * Created by wandersonblough on 1/14/16.
 */
public abstract class AppBaseDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CheckInPresenter.ViewModel, CountPresenter.ViewModel, LocationReceiver {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int MIN_COUNT = 0;
    private final int MAX_COUNT = 99;
    private static final String TAG = AppBaseDrawerActivity.class.getSimpleName();

    @Bind(R.id.main_content)
    CoordinatorLayout mainContent;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.navigation_drawer)
    NavigationView navigationView;

    @Bind(R.id.sync_view)
    SyncProgressView syncProgressView;

    @Bind(R.id.simple_sync_view)
    SimpleSyncProgressView simpleSyncProgressView;

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private CountPresenter countPresenter;
    private Button syncErrorButton;
    private Menu menu;
    private ActionBarDrawerToggle drawerToggle;
    private SyncListener syncListener;
    private SyncListener mSyncListenerForChecking;
    private Subscription logoutSubscription = Subscriptions.empty();
    private Subscription permissionSubscription = Subscriptions.empty();
    private CheckInPresenter checkInPresenter;
    private Snackbar snackbar;

    protected boolean syncInProgress;
    private DownDialog downDialog;
    BroadcastReceiver syncEngineBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DataManager.SYNC_COMPLETED:
                    if (syncListener != null) {
                        syncListener.onSyncCompleted();
                    }

                    if(mSyncListenerForChecking != null){
                        mSyncListenerForChecking.onSyncCompleted();
                    }

                    Log.d(TAG, "onReceive: SYNC COMPLETED");
                    syncInProgress = false;
                    onSyncCompleted();
                    onRefresh();
                    countPresenter.start();
                    configureDrawerBasedOnPermissions();
                    invalidateOptionsMenu();
                    break;
                case DataManager.DYNAMIC_FETCH_COMPLETED:
                    Log.d(TAG, "onReceive: SYNC DYNAMIC_FETCH_COMPLETED");
                    onSyncCompleted();
                    dynamicFetchCompleted(intent);
                    break;
                case DataManager.SYNC_STARTED:
                    Log.d(TAG, "onReceive: SYNC STARTED");
                    syncInProgress = true;
                    onSyncStart();
                    break;
                case DataManager.SYNC_ENGINE_ERROR:
                    if (syncListener != null) {
                        syncListener.onSyncError(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    }

                    if(mSyncListenerForChecking != null){
                        mSyncListenerForChecking.onSyncError(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    }
                    countPresenter.start();
                    break;
                case DataManager.SYNC_ENGINE_FAILURE:
                    Log.d(TAG, "onReceive: SYNC FAILURE");
                    if (syncListener != null) {
                        syncListener.onSyncFailure(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    }

                    if(mSyncListenerForChecking != null){
                        mSyncListenerForChecking.onSyncFailure(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    }
                    syncInProgress = false;
                    break;
                case DataManager.FETCH_CONTENT_COMPLETED:
                    Log.d(TAG, "onReceive: FETCH CONTENT COMPLETED");
                    break;
                case ClientManager.ACCESS_TOKEN_REFRESH_INTENT:
                    Log.d(TAG, "onReceive: ACCESS_TOKEN_REFRESH_INTENT");
                    break;
                case ClientManager.INSTANCE_URL_UPDATE_INTENT:
                    Log.d(TAG, "onReceive: INSTANCE_URL_UPDATE_INTENT");
                    break;
                case ClientManager.ACCESS_TOKEN_REVOKE_INTENT:
                    // TODO: We should redirect the user to login activity in this case
                    Log.d(TAG, "onReceive: ACCESS_TOKEN_REVOKE_INTENT");
                    break;
                default:
            }
        }
    };



    public void setSyncListenerForChecking(SyncListener mSyncListenerForChecking) {
        this.mSyncListenerForChecking = mSyncListenerForChecking;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getPreferredOrientation());
        setContentView(R.layout.drawer_activity_layout);
        mainContent = (CoordinatorLayout) findViewById(R.id.main_content);
        View view = LayoutInflater.from(this).inflate(getLayoutResId(), mainContent, attachToRoot());
        if (!attachToRoot()) {
            mainContent.addView(view);
        }
        ButterKnife.bind(this);

        configureDrawerBasedOnPermissions();

        setupDefaultToolbar();

        navigationView.setNavigationItemSelectedListener(this);

        try {
            syncProgressView.initDataManger();
        } catch (Exception e) {
            // Figure out a cleaner way to handle AccountInfoNotFoundException and other exceptions
            // that can cause during data initialization
            // the below toast is not really visible since finish gets called right away
            // Toast.makeText(this, R.string.data_init_error, Toast.LENGTH_SHORT);
            finish();
        }
        syncListener = syncProgressView;
        countPresenter = new CountPresenter();
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        //fetch latest app version
        DynamicFetchEngine.fetchInBackground(ABInBevApp.getAppContext(), AbInBevConstants.DynamicFetch.CHECK_LATEST_VERSION, null);
    }

    protected void setSimpleSyncProgress(SyncStatus status) {
        if (status == null || SyncStatusState.COMPLETED.equals(status.getStatus())) {
            simpleSyncProgressView.setVisibility(View.GONE);
        } else {
            simpleSyncProgressView.setVisibility(View.VISIBLE);
            simpleSyncProgressView.setSyncStatus(status);
        }
    }

    protected void setupDefaultToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    ViewUtils.closeKeyboard(drawerView);
                    super.onDrawerSlide(drawerView, slideOffset);
                }
            };

            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.syncState();
            drawerLayout.setDrawerListener(drawerToggle);
        }
    }

    private int getPreferredOrientation() {
        return getResources().getBoolean(R.bool.is10InchTablet) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    /**
     * Shows or hides drawer menuItems based on the
     * current user's permissions.
     */
    private void configureDrawerBasedOnPermissions() {
        permissionSubscription.unsubscribe();
        permissionSubscription = Observable.fromCallable(PermissionManager::getInstance)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        permissionManager -> {
                            Menu menu = navigationView.getMenu();
                            menu.findItem(R.id.visit_list)
                                    .setVisible(permissionManager.hasPermission(VISIT_LIST));
                            menu.findItem(R.id.user_details)
                                    .setVisible(permissionManager.hasPermission(USER_360));
                            menu.findItem(R.id.prospects_list)
                                    .setVisible(permissionManager.hasPermission(PROSPECT_LIST));
                            menu.findItem(R.id.account_list)
                                    .setVisible(permissionManager.hasPermission(ACCOUNTS));
                            menu.findItem(R.id.quiz_list)
                                    .setVisible(permissionManager.hasPermission(QUIZZES));
                            menu.findItem(R.id.dsa)
                                    .setVisible(permissionManager.hasPermission(DSA));
                            menu.findItem(R.id.chatter)
                                    .setVisible(permissionManager.hasPermission(CHATTER));
                            menu.findItem(R.id.calculator)
                                    .setVisible(permissionManager.hasPermission(CALCULATOR));
                            menu.findItem(R.id.full_sync)
                                    .setVisible(permissionManager.hasPermission(FULL_SYNC));

                        },
                        error -> Log.w(TAG, error)
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        countPresenter.setViewModel(this);
        registerSyncEngineReceiver();
        syncProgressView.resume();
        if (!syncProgressView.isInProgress()) {
            countPresenter.start();
            onRefresh();
        }

        // if we go to onPause when the sync completes then we are in the wrong state without the below code
        if (SyncEngine.getSyncStatus().getStatus() == SyncStatusState.INPROGRESS) {
            syncInProgress = true;
        } else {
            syncInProgress = false;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void setPendingCount(int count) {
        TextView textView = (TextView) navigationView.getMenu().findItem(R.id.pending_sync).getActionView().findViewById(R.id.count);
        textView.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        textView.setText(String.valueOf(count));
    }

    @Override
    public void setErrorCount(int count) {
        if (menu != null) {
            menu.findItem(R.id.menu_sync_error).setVisible(count != 0);
            syncErrorButton.setText(String.valueOf(count));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncEngineBroadcastReceiver);
        if (checkInPresenter != null) {
            checkInPresenter.stop();
        }
        if (countPresenter != null) {
            countPresenter.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        DownLoadUtils.getInstance().clearAllRequest();

        if (AppVersionUpgrade.getInstance().getDownDialog() != null)
            AppVersionUpgrade.getInstance().getDownDialog().dismiss();
        AppVersionUpgrade.getInstance().setDownDialog(null);
        super.onDestroy();

        permissionSubscription.unsubscribe();
    }

    private void registerSyncEngineReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataManager.SYNC_ENGINE_ERROR);
        intentFilter.addAction(DataManager.SYNC_COMPLETED);
        intentFilter.addAction(DataManager.DYNAMIC_FETCH_COMPLETED);
        intentFilter.addAction(DataManager.SYNC_STARTED);
        intentFilter.addAction(DataManager.SYNC_ENGINE_FAILURE);
        intentFilter.addAction(DataManager.FETCH_CONTENT_COMPLETED);
        intentFilter.addAction(ClientManager.ACCESS_TOKEN_REFRESH_INTENT);
        intentFilter.addAction(ClientManager.INSTANCE_URL_UPDATE_INTENT);
        intentFilter.addAction(ClientManager.ACCESS_TOKEN_REVOKE_INTENT);
        registerReceiver(syncEngineBroadcastReceiver, intentFilter);
    }

    public AppComponent getAppComponent() {
        return ((ABInBevApp) getApplication()).getAppComponent();
    }

    public abstract int getLayoutResId();

    protected boolean attachToRoot() {
        return false;
    }
    protected void dynamicFetchCompleted(Intent intent) {
        invalidateOptionsMenu();
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        Intent intent = null;

        switch (menuItem.getItemId()) {
            case R.id.user_details:
                intent = new Intent(this, UserDetailsActivity.class)
                        .putExtra(UserDetailsActivity.ARG_IS_OPENED_FROM_DRAWER, true);
                break;
            case R.id.visit_list:
                intent = new Intent(this, VisitPlanActivity.class);
                break;
            case R.id.prospects_list:
                intent = new Intent(this, ProspectListActivity.class);
                TaskStackBuilder.create(this)
                        .addParentStack(ProspectListActivity.class)
                        .addNextIntent(intent)
                        .startActivities();
                return true;
            case R.id.account_list:
                intent = new Intent(this, AccountListActivity.class);
                TaskStackBuilder.create(this)
                        .addParentStack(AccountListActivity.class)
                        .addNextIntent(intent)
                        .startActivities();
                return true;
            case R.id.case_list:
                intent = new Intent(this, AccountCasesListActivity.class);
                intent.putExtra(AccountCasesListActivity.DISPLAY_USER_CASES, true);
                TaskStackBuilder.create(this)
                        .addParentStack(AccountCasesListActivity.class)
                        .addNextIntent(intent)
                        .startActivities();
                return true;
            case R.id.dsa:
                intent = new Intent(this, DigitalSalesAid.class);
                TaskStackBuilder.create(this)
                        .addParentStack(DigitalSalesAid.class)
                        .addNextIntent(intent)
                        .startActivities();
                return true;
            case R.id.chatter:
                intent = new Intent(this, ChatterWebViewActivity.class);
                intent.putExtra(ChatterWebViewActivity.URL, getString(R.string.chatter_newsfeed_url_suffix));
                break;
            case R.id.system_info:
                closeDrawer();
                showAboutDialog();
                break;
            case R.id.pending_sync:
                intent = new Intent(this, SyncPendingListActivity.class);
                TaskStackBuilder.create(this)
                        .addParentStack(SyncPendingListActivity.class)
                        .addNextIntent(intent)
                        .startActivities();
                return true;
            case R.id.survey_test:
                intent = new Intent(this, SurveyTestActivity.class);
//                int fileNum = clearLocalContent(this);
//                Toast.makeText(this, "Deleted files: " + fileNum, Toast.LENGTH_LONG).show();
                break;
            case R.id.quiz_list:
                intent = new Intent(this, QuizListActivity.class);
                TaskStackBuilder.create(this)
                        .addParentStack(QuizListActivity.class)
                        .addNextIntent(intent)
                        .startActivities();
                return true;
            case R.id.calculator:
                intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
                intent.setClassName("com.android.calculator2",
                        "com.android.calculator2.Calculator");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case R.id.logout:
                closeDrawer();
                if (syncInProgress) {
                    Toast.makeText(this, R.string.wait_sync_in_progress, Toast.LENGTH_LONG).show();
                } else {
                    promptLogout();
                }
                break;
            case R.id.full_sync:
                closeDrawer();
                if (syncInProgress) {
                    Toast.makeText(this, R.string.wait_sync_in_progress, Toast.LENGTH_LONG).show();
                } else {
                    promptFullSync();
                }
        }
        if (intent != null) {
            try {
                closeDrawer();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Unable to handle activity: " + e);
            }
            return true;
        } else {
            return false;
        }
    }

    private int deleteDir(File dir) {
        int files = 0;
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
                files++;
            } else {
                files += deleteDir(file);
            }
        }

        return files;
    }

    public int clearLocalContent(Context context) {
        // delete content files
        return deleteDir(context.getFilesDir());
    }

    private void closeDrawer() {
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.drawer_activity_menu, menu);

        MenuItem messageItem = menu.findItem(R.id.notifycation_message);
        MenuItemCompat.setActionView(messageItem, R.layout.message_unread_count);
        FrameLayout frameLayout = (FrameLayout) messageItem.getActionView();
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message(v);
            }
        });

        MenuItem menuItem = menu.findItem(R.id.menu_sync_error);
        MenuItemCompat.setActionView(menuItem, R.layout.sync_error_count);
        syncErrorButton = (Button) menuItem.getActionView();
        syncErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppBaseDrawerActivity.this, SyncErrorListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        this.menu = menu;
        setErrorCount(0);
        return true;
    }

    //Message Notifycation Page
    private void message(View v) {
        Intent intent = new Intent(this, NotifyMessageActivity.class);
        TaskStackBuilder.create(this)
                .addParentStack(NotifyMessageActivity.class)
                .addNextIntent(intent)
                .startActivities();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        countPresenter.start();
        changeMessageCount(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     *Check the number of unread messages
     * @param menu
     */
    public synchronized void changeMessageCount(Menu menu) {

        try {

            int count = CN_Notification_Message__c.getUnReadRecoderCount();
            MenuItem messageItem = menu.findItem(R.id.notifycation_message);
            FrameLayout frameLayout = (FrameLayout) messageItem.getActionView();
            TextView textView = (TextView) frameLayout.findViewById(R.id.unread_count);
            if (count > MIN_COUNT) {
                if (count <= MAX_COUNT)
                    textView.setText(""+count);
                else
                    textView.setText("···");
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sync) {
            if (syncInProgress) {
                Toast.makeText(this, R.string.wait_sync_in_progress, Toast.LENGTH_LONG).show();
            } else {
                syncProgressView.startSync(false, false);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!syncProgressView.isInProgress()) {
            super.onBackPressed();
        }
    }

    @Override /* CheckInPresenter.ViewModel */
    public void showCheckOutPrompt(Account account) {
        String message = getString(R.string.banner_message, account.getName());
        snackbar = Snackbar.make(mainContent, message, Snackbar.LENGTH_INDEFINITE);

        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (textView != null) textView.setMaxLines(2);  // show multiple line

        snackbar.setAction(R.string.check_out, v -> {
            checkInPresenter.onCheckOutClicked();
            snackbar.dismiss();
        });

        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.sab_yellow));
        snackbar.show();
    }

    @Override /* CheckInPresenter.ViewModel */
    public void onCheckOutFinished() {

    }



    public SyncProgressView getSyncProgressView() {
        return syncProgressView;
    }

    public void setSyncListener(SyncListener syncListener) {
        this.syncListener = syncListener;
    }

    public abstract void onRefresh();

    protected void onSyncCompleted() {
        AppVersionUpgrade.getInstance().checkAppVersion(this);
    }

    public void onSyncStart() {

    }

    private void showAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.app_name);
        builder.setCancelable(true);

        builder.setMessage(Html.fromHtml(getAppInfo()));

        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(R.string.more_info_english,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(AppBaseDrawerActivity.this, DiagnosisActivity.class);
                        startActivity(intent);
                    }
                });
        builder.show();

    }

    private String getAppInfo() {

        ClientManager clientManager = new ClientManager(this, SalesforceSDKManager.getInstance()
                .getAccountType(), SalesforceSDKManager.getInstance().getLoginOptions(), SalesforceSDKManager
                .getInstance().shouldLogoutWhenTokenRevoked());

        String databaseSize = android.text.format.Formatter.formatShortFileSize(this, DataManagerFactory.getDataManager().getSmartStore().getDatabaseSize());

        RestClient client = clientManager.peekRestClient();

        String userName = client.getClientInfo().username;

        StringBuffer appInfo = new StringBuffer();

        appInfo.append("<br/>");
        appInfo.append(getString(R.string.html_about_row, getString(R.string.app_version), BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"));

        appInfo.append("<br/>");
        appInfo.append(getString(R.string.html_about_row, getString(R.string.user), userName));
        appInfo.append(getString(R.string.html_about_row, getString(R.string.database_size), databaseSize));

        appInfo.append("<br/>");
        appInfo.append(getString(R.string.html_about_row, getString(R.string.android_version), Build.VERSION.RELEASE));
        appInfo.append(getString(R.string.html_about_row, getString(R.string.device_name), Build.MODEL));

        appInfo.append("<br/>");
        appInfo.append(getString(R.string.html_about_row, getString(R.string.available_storage), DataUtils.getAvailableInternalMemorySize(this)));
        appInfo.append(getString(R.string.html_about_row, getString(R.string.total_storage), DataUtils.getTotalInternalMemorySize(this)));

        return appInfo.toString();

    }

    private void promptFullSync() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.full_sync))
                .setMessage(getString(R.string.abi_full_sync_message))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        syncProgressView.startSync(true, true);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private void promptLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.abi_logout))
                .setMessage(getString(R.string.abi_logout_message))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        logoutUser();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void checkAccount(String accountId) {
        if (checkInPresenter == null) {
            checkInPresenter = new CheckInPresenter(this, accountId);
        } else {
            checkInPresenter.setCurrentAccountId(accountId);
        }
        checkInPresenter.setViewModel(this);
        checkInPresenter.start();
    }

    public void dismissSnackbar() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    public void logoutUser() {
        syncProgressView.setLoggingOut();
        logoutSubscription.unsubscribe();
        logoutSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                ClientManager clientManager = new ClientManager(AppBaseDrawerActivity.this, SalesforceSDKManager.getInstance().getAccountType(),
                        SalesforceSDKManager.getInstance().getLoginOptions(),
                        SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

                RestClient client = clientManager.peekRestClient();
                DataManager dataManager = DataManagerFactory.getDataManager();
                if (dataManager != null) {
                    dataManager.clearLocalData(AppBaseDrawerActivity.this, true);
                    dataManager.logout(AppBaseDrawerActivity.this, client);
                }
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        SmartStoreSDKManager.getInstance().logout(AppBaseDrawerActivity.this, false);
                        Intent intent = new Intent(AppBaseDrawerActivity.this, LoginActivity.class);
                        getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public Activity getReceivingActivity() {
        return this;
    }

    @Override
    public int getFailureResolutionRequestCode() {
        return CONNECTION_FAILURE_RESOLUTION_REQUEST;
    }

    @Override
    public void handleUnresolvedError(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        String errorMessageString = GooglePlayServicesUtil.getErrorString(errorCode);
        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Show the error dialog in the DialogFragment
            errorDialog.show();
        }
        Toast.makeText(this, errorMessageString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewLocationReceived(Location location) {
        //no action
    }

    @Override
    public void onConnected() {
        //no action
    }
}
