/*
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dsa.app.ui.activity;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.model.CN_DSA_Azure_File_Usage__c;
import com.salesforce.dsa.app.sync.CheckAccessFilePermissionTask;
import com.salesforce.dsa.data.model.CN_DSA_Folder__c;
import com.salesforce.dsa.data.model.CN_DSA__c;
import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsdk.util.EventsObservable;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueOperation;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.dsa.DSAAppState;
import com.abinbev.dsa.R;
import com.salesforce.dsa.app.sync.DownloadDsaFileTask;
import com.salesforce.dsa.app.ui.adapter.CategoryListAdapter;
import com.salesforce.dsa.app.ui.adapter.ContentListAdapter;
import com.salesforce.dsa.app.ui.customview.SyncProgressDialog;
import com.salesforce.dsa.app.ui.fragments.DsaFileFolderFragment;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.app.utils.DeviceNetworkUtils;
import com.salesforce.dsa.async.ModelDataFetcherTask;
import com.salesforce.dsa.data.model.CategoryMobileConfig__c;
import com.salesforce.dsa.data.model.Category__c;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.data.model.TrackedDocument;
import com.salesforce.dsa.utils.CategoryUtils;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DisplayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main activity.
 *
 * @author bduggirala, usanaga
 */
public class DigitalSalesAid extends BaseActivity implements OnQueryTextListener, OnClickListener, DownloadDsaFileTask.DownloadDsaFileCallBack {

    private static final String TAG = "DigitalSalesAid";

    private DataManager dataManager;
    private RestClient client;

    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     * <p>
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object syncObserverHandle;

    private SyncProgressDialog progressDialog;
    private boolean syncInProgress;

    private Toast toast;
    private Menu optionsMenu;

    private FrameLayout rootLayout;
    private ImageView backgroundImageView;


    private List<CN_DSA__c> activeConfigList;
    private List<Category__c> activeCategoryList = new ArrayList<>();
    private CN_DSA__c activeConfig;
    //    private MobileAppConfig__c activeConfig;
    private Category__c activeCategory;

    private FrameLayout buttonViewContainer;
    private FrameLayout visualViewContainer;
    private RelativeLayout visualMenuContainer;
    private RelativeLayout visualContentContainer;
    private ImageView categoryBackgroundImageView;
    private View categoryOverlay;

    private boolean alreadyInResumeClient = false;
    private CategoryLoaderTask categoryLoaderTask;
    private File targetFile, tempFile;
    public static String TEMP_SUFFIX = ".temp_file";
    private DownloadDsaFileTask.DownloadDsaFileCallBack downloadDsaFileCallBack;
    private OnBackPressed onBackPressed;
    private DsaFileFolderFragment dsaFileFolderFragment;

    public interface OnBackPressed {
        void onBackPressed();
    }

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

    public void setDownloadDsaFileCallBack(DownloadDsaFileTask.DownloadDsaFileCallBack downloadDsaFileCallBack) {
        this.downloadDsaFileCallBack = downloadDsaFileCallBack;
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backgroundImageView = (ImageView) findViewById(R.id.background_image);

        rootLayout = (FrameLayout) findViewById(R.id.root);
        buttonViewContainer = (FrameLayout) findViewById(R.id.button_view_container);
        visualViewContainer = (FrameLayout) rootLayout.findViewById(R.id.visual_view_container);
        categoryBackgroundImageView = (ImageView) visualViewContainer.findViewById(R.id.category_background_image);
        visualMenuContainer = (RelativeLayout) visualViewContainer.findViewById(R.id.visual_menu_container);
        visualContentContainer = (RelativeLayout) visualViewContainer.findViewById(R.id.visual_content_container);
        categoryOverlay = visualViewContainer.findViewById(R.id.category_overlay);

        // Avoids having to put null checks
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        DisplayUtils.updateDeviceDisplayValues(this);

        activeConfigList = new ArrayList<>();

        visualViewContainer.setOnClickListener(this);
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.initial_layout;
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.home_drawer_item;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "in onResume");

        // Hide the view until we are logged in.
        rootLayout.setVisibility(View.INVISIBLE);
        getClientAndResume();

        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        syncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        // Register to receive errors
        registerSyncEngineReceiver();

        registerDSAReceiver();

        setCheckInOutButton(optionsMenu);

        // call stopviewingdocument here as there is no built in viewer
        DSAAppState.getInstance().stopViewingDocument(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ImageView iv = (ImageView) findViewById(R.id.redBorder);
        if (prefs.getBoolean(DSAConstants.Constants.INTERNAL_MODE, false)) {
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "in onPause");
        if (syncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(syncObserverHandle);
            syncObserverHandle = null;
        }

        alreadyInResumeClient = false;
        unregisterReceiver(syncEngineBroadcastReceiver);
        //unregisterReceiver(dsaBroadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerDSAReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(dsaBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.e(TAG, "in onConfigurationChagned");
        DisplayUtils.updateDeviceDisplayValues(this);
//        updateUI();
    }

    private void onResume(RestClient client) {
        Log.i(TAG, "in onResume RestClient");

        this.client = client;
        dataManager = DataManagerFactory.getDataManager();

        if (progressDialog == null)
            progressDialog = new SyncProgressDialog(this);

        try {
            // Show the view.
            rootLayout.setVisibility(View.VISIBLE);

            if (alreadyInResumeClient) {
                return;
            } else {
                alreadyInResumeClient = true;
            }

            if (dataManager.isFirstSyncComplete(this)) {

                // TODO: this needs to happen after a sync, not for every launch
                if (categoryLoaderTask == null) {
                    //task get all folders
                    categoryLoaderTask = new CategoryLoaderTask();

//                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DigitalSalesAid.this);
//                    String activeConfigId = prefs.getString(DSAConstants.Constants.ACTIVE_CONFIG_ID, null);
//                    if (activeConfigId != null) {
//                        categoryLoaderTask.execute(0);
//                        activeConfig = DataUtils.fetchMobileAppConfigForId(activeConfigId);
//                        if (activeConfig != null) {
////                            DataUtils.fetchCategoryMobileConfig(activeConfig, catMobileConfigCallback);
//                            attachFragment();
//                        }
//                    } else {
//                        if (progressDialog.isShowing()) {
//                            progressDialog.dismiss();
//                        }
                    showSelectConfigsDialog();
//                    }
                }
            } else {
                // Init should always be called first before any other calls are made
                dataManager.init(this);
                progressDialog.show();
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Manifest error: ", e);
            Toast.makeText(this, "Invalid manifest file provided. Please check logcat for errors.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Log.e(TAG, "resetting clientAppVersion ");
            //PreferenceUtils.putClientAppVersion(0, this);
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (onBackPressed != null)
            onBackPressed.onBackPressed();
        if (dsaFileFolderFragment == null || dsaFileFolderFragment.isHidden()) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        optionsMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the SearchView and set the searchable configuration
        ComponentName searchCompName = new ComponentName(this, SearchResultsActivity.class.getName());
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(searchCompName));
//	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setCheckInOutButton(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            //		case R.id.menu_refresh:
            //			SyncUtils.TriggerRefresh(this);
            //			return true;
            //		case R.id.menu_clear:
            //			onClearClick();
            //			return true;
            //		case R.id.menu_logout:
            //			onLogout();
            //			return true;
//            case R.id.menu_checkin:
//                handleCheckInButtonClick();
//                return true;
            case R.id.menu_history:
                List<ContentVersion> history = DSAAppState.getInstance().getHistoryItems();
                if (history.size() > 0) {
                    Intent historyIntent = new Intent(this, ContentHistoryActivity.class);
                    startActivity(historyIntent);
                } else {
                    Toast.makeText(this, "There are no items in history. Please select an item in browse or search", Toast.LENGTH_LONG).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getClientAndResume() {

        // work around to avoid a memory leak when we call getRestClient with the activity

        try {
            ClientManager clientManager = new ClientManager(getApplicationContext(), SalesforceSDKManager.getInstance().getAccountType(),
                    SalesforceSDKManager.getInstance().getLoginOptions(),
                    SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

            RestClient client = clientManager.peekRestClient();
            if (client != null) {
                onResume(client);
                return;
            }
        } catch (Exception e) {
            // continue with the below flow if we get any exceptions or if the client is null
        }


        // Gets login options.
        final String accountType = SalesforceSDKManager.getInstance().getAccountType();
        final ClientManager.LoginOptions loginOptions = SalesforceSDKManager.getInstance().getLoginOptions();

        // Gets a rest client.
        new ClientManager(getApplicationContext(), accountType, loginOptions,
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked()).getRestClient(this, new ClientManager.RestClientCallback() {

            @Override
            public void authenticatedRestClient(RestClient client) {
                if (client == null) {
                    finish();
                    return;
                }
                onResume(client);

                // Lets observers know that rendition is complete.
                EventsObservable.get().notifyEvent(EventsObservable.EventType.RenditionComplete);
            }
        });
    }

    private void handleCheckInButtonClick() {

        if (DSAAppState.getInstance().isCheckout()) {
            List<TrackedDocument> tds = DSAAppState.getInstance().getTrackedDocuments();
            if (tds.size() > 0) {
                Intent checkoutIntent = new Intent(this, CheckoutReviewActivity.class);
                startActivity(checkoutIntent);
            } else {
                DSAAppState.getInstance().setTrackingType(DSAAppState.DocumentTrackingType.DocumentTracking_None);
                setCheckInOutButton(optionsMenu);
            }
        } else {
            DSAAppState.getInstance().setCheckInStart(new Date());
            Intent checkinIntent = new Intent(this, CheckinSelectContactActivity.class);
            checkinIntent.putExtra("showChooseLaterButton", true);
            startActivity(checkinIntent);
        }
    }

    private void setCheckInOutButton(Menu menu) {
//        if (menu == null || menu.findItem(R.id.menu_checkin) == null)
//            return;
//        if (DSAAppState.getInstance().isCheckout()) {
//
//            menu.findItem(R.id.menu_checkin).setIcon(R.drawable.ic_action_checkout);
//        } else {
//            menu.findItem(R.id.menu_checkin).setIcon(R.drawable.ic_action_checkin);
//        }
    }

    public void onLogout() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Editor editor = prefs.edit();
        editor.putBoolean("SHOW_SELECT_CONFIGS", true);
        editor.putString(DSAConstants.Constants.ACTIVE_CONFIG_ID, null);
        editor.commit();

        dataManager.logout(this, client);
        SmartStoreSDKManager.getInstance().logout(this);
        progressDialog = null;
    }

    public void onViewChange(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        boolean isMenuBrowser = fm.getBackStackEntryCount() > 0;
        if (isMenuBrowser) {
            fm.popBackStack();
        } else {
            // dont let the user switch if there is no active config
            if (activeConfig != null) {
//                attachFragment();
            }
        }
    }

    private void attachFragment() {
//        MenuBrowserFragment menuBrowserFragment = new MenuBrowserFragment();
        dsaFileFolderFragment = new DsaFileFolderFragment();
        Bundle b = new Bundle();
        b.putString(DSAConstants.Constants.ACTIVE_CONFIG_ID, activeConfig.getId());
        dsaFileFolderFragment.setArguments(b);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.root, dsaFileFolderFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    private void updateUI() {
        if (activeConfig == null) {
            Toast.makeText(this, "No active configurations!! Please wait for sync to complete!!", Toast.LENGTH_LONG).show();
            return;
        }

        // this can be triggered on force full sync
        if (!DataManagerFactory.getDataManager().isFirstSyncComplete(this)) {
            if (syncInProgress)
                return;
        }

        updateLogo();
        updateBackground();
        updateButtons();
        updateCategoryBackground();
    }

    private void updateBackground() {
//        String backgroundImageId;
//
//        int currentOrientation = getResources().getConfiguration().orientation;
//
//        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
//            backgroundImageId = activeConfig.getLandscapeAttachmentId__c();
//        } else {
//            backgroundImageId = activeConfig.getPortraitAttachmentId__c();
//        }
//
//        try {
//            // Drawable backgroundDrawable = Drawable.createFromStream(getAssets().open("images/" + configData.backgroundFileName), null);
//
//            Drawable backgroundDrawable = ContentUtils.getDrawableFromFileId(this, backgroundImageId);
//
//            if (backgroundDrawable == null) {
//                // set default background
//                backgroundImageView.setImageDrawable(getResources().getDrawable(R.drawable.bg_default_landscape));
//            } else {
//                backgroundImageView.setImageDrawable(backgroundDrawable);
//                Log.i(TAG, "backgroundImageView w:h " + backgroundDrawable.getMinimumWidth() + ":" + backgroundDrawable.getMinimumHeight());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void updateCategoryBackground() {

        String backgroundImageId;

        if (activeCategory == null) return;

        CategoryMobileConfig__c categoryConfig = DataUtils.fetchCategoryConfigForCategoryAndAppConfigId(activeCategory, activeConfig.getId());

        int currentOrientation = getResources().getConfiguration().orientation;

        Drawable backgroundDrawable = ContentUtils.getCategoryBackgroundDrawableForOrientation(categoryConfig, currentOrientation, this);
        categoryBackgroundImageView.setImageDrawable(backgroundDrawable);
    }

    private void updateLogo() {
//        Drawable logoDrawable = ContentUtils.getDrawableFromFileId(this, activeConfig.getLogoAttachmentId__c());

//		if (logoDrawable == null) {
//			actionBar.setLogo(R.drawable.default_logo_small);
//		} else {
//			actionBar.setLogo(logoDrawable);
//		}
    }

    private void updateButtons() {
//
//        int count = buttonViewContainer.getChildCount();
//
//        // remove existing views
//        for (int i = 0; i < count; i++) {
//            View view = buttonViewContainer.findViewWithTag(i);
//            if (view == null) break;
//            else {
//                buttonViewContainer.removeView(view);
//            }
//        }
//
//        int currentOrientation = getResources().getConfiguration().orientation;
//
//        double buttonWidth = 0;
//        double buttonHeight = 0;
//
//        String defaultButtonImageId = activeConfig.getButtonDefaultAttachmentId__c();
//
//        Drawable defaultButtonDrawable = ContentUtils.getDrawableFromFileId(this, defaultButtonImageId);
//
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//
//
//        if (defaultButtonDrawable == null) {
//            // set default button
//            defaultButtonDrawable = getResources().getDrawable(R.drawable.default_empty_button);
//        } else {
//            buttonWidth = metrics.density * defaultButtonDrawable.getIntrinsicWidth();
//            buttonHeight = metrics.density * defaultButtonDrawable.getIntrinsicHeight();
//        }

//        String highlightButtonImageId = activeConfig.getButtonHighlightAttachmentId__c();
//        Drawable highlightedButtonDrawable = ContentUtils.getDrawableFromFileId(this, highlightButtonImageId);

//        if (highlightedButtonDrawable == null) {
//            // set default button
//            highlightedButtonDrawable = defaultButtonDrawable;
//        }
//
//        boolean isTransparentButton;
//
//        float alpha = ContentUtils.getAlphaFromString(activeConfig.getButtonTextAlpha__c(), 100);
//        isTransparentButton = alpha < 1;
//
//        Log.i(TAG, "using Transparent Buttons: " + isTransparentButton);

        // add new views

//        int size = activeCategoryList.size();
//        Log.e(TAG, "active config list Size: " + size);
//
//        for (int i = 0; i < size; i++) {
//            Category__c category = activeCategoryList.get(i);
//            CategoryMobileConfig__c categoryConfig = DataUtils.fetchCategoryConfigForCategoryAndAppConfigId(category, activeConfig.getId());
//
//            RelativeLayout mainButtonItem = (RelativeLayout) (getLayoutInflater().inflate(R.layout.main_button_item, buttonViewContainer, false));
//
//            ImageView btnImageView = (ImageView) mainButtonItem.findViewById(R.id.button_image_view);
//            btnImageView.setImageDrawable(ContentUtils.getStateListDrawable(defaultButtonDrawable, highlightedButtonDrawable));

//            TextView btnTextView = (TextView) mainButtonItem.findViewById(R.id.button_item_title);
//
//            float textAlpha = ContentUtils.getAlphaFromString(activeConfig.getButtonTextAlpha__c(), 100);
//            int defaultColor = ContentUtils.getColorFromString(activeConfig.getButtonTextColor__c(), Color.BLACK);
//            int highlightedColor = ContentUtils.getColorFromString(activeConfig.getButtonHighlightTextColor__c(), Color.BLUE);

//            btnTextView.setText(category.getName());
//            btnTextView.setTextColor(ContentUtils.getColorStateList(defaultColor, highlightedColor));
//            btnTextView.setAlpha(textAlpha);
//            btnTextView.setGravity(ContentUtils.getGravityFromString(categoryConfig.getButton_Text_Align__c(), Gravity.CENTER));

//            btnImageView.setContentDescription(category.getName());
//            double xValue;
//            double yValue;

//            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
//                xValue = categoryConfig.getLandscapeX__c();
//                yValue = categoryConfig.getLandscapeY__c();
//            } else {
//                xValue = categoryConfig.getPortraitX__c();
//                yValue = categoryConfig.getPortraitY__c();
//            }
//
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(DisplayUtils.calculatedWidth(buttonWidth, isTransparentButton), DisplayUtils.calculatedHeight(buttonHeight, isTransparentButton));
//            layoutParams.leftMargin = DisplayUtils.calculatedLeftMargin(xValue, buttonWidth);
//            layoutParams.topMargin = DisplayUtils.calculatedTopMargin(yValue, buttonHeight);
//            mainButtonItem.setTag(i);
//            mainButtonItem.setTag(R.id.TAG_TITLE_ID, category);
//            mainButtonItem.setLayoutParams(layoutParams);
//            mainButtonItem.setOnClickListener(this);
//            mainButtonItem.setContentDescription(category.getName());
//            buttonViewContainer.addView(mainButtonItem);
//        }
    }

    private void logErrors() {
        List<ErrorObject> errorList = dataManager.getErrors();
        boolean deleteQueue = true;
        for (ErrorObject errorObject : errorList) {
            Log.e("SyncEngineErrors", errorObject.toString());

            try {

                if (errorObject.getQueueSoupEntryId() != null) {
                    QueueObject queueObject = dataManager
                            .getQueueRecordFromClient(errorObject
                                    .getQueueSoupEntryId());

                    if (queueObject != null) {
                        QueueOperation queueOperation = queueObject.getOperation();
                        Log.e("SyncEngineErrors", "queueEntry is: "
                                + queueObject.toJson().toString());

                        String errorMessage = "Problem";
                        switch (queueOperation) {
                            case CREATE:
                                errorMessage = "Problem creating ";
                                break;
                            case DELETE:
                                errorMessage = "Problem deleting ";
                                break;
                            case UPDATE:
                                errorMessage = "Problem updating ";
                                break;
                            default:
                                break;
                        }

                        toast = Toast.makeText(DigitalSalesAid.this, errorMessage + queueObject.getObjectType() + " on server."
                                        + "\nError: " + errorObject.getErrorMessage()
                                        + "\nRetry count: " + queueObject.getRetryCount(),
                                Toast.LENGTH_LONG);
                        toast.show();

                        // Try twice and then delete
                        if (deleteQueue && queueObject.getRetryCount() > 1) {
                            Log.i(TAG, "Deleting the following object in Queue: "
                                    + queueObject.toJson());
                            dataManager.deleteQueueRecordFromClient(
                                    errorObject.getQueueSoupEntryId(), true);
                        }
                    }
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception in logErrors");
                e.printStackTrace();
            }

        }

    }

    //	private void onClearClick() {
    //		dataManager.clearLocalData(this);
    //	}

    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    public void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = optionsMenu.findItem(R.id.menu_refresh);
        refreshItem.setVisible(refreshing);
        if (refreshItem != null) {
            if (refreshing) {
                syncInProgress = true;
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                if (syncInProgress) {
                    syncInProgress = false;

                    if (dataManager.isFirstSyncComplete(this)) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                }
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync pedidoListAdapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = SyncUtils.getAccount(getApplicationContext());
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync pedidoListAdapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, SyncUtils.getContentAuthority(getApplicationContext()));
                    //                    boolean syncPending = ContentResolver.isSyncPending(
                    //                            account, SyncUtils.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive);
                }
            });
        }
    };

    private void registerSyncEngineReceiver() {
        Log.i(TAG, "in registerSyncErrorReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataManager.SYNC_ENGINE_ERROR);
        intentFilter.addAction(DataManager.SYNC_ENGINE_CONTENT_FILES_RECEIVED);
        intentFilter.addAction(DataManager.SYNC_COMPLETED);
        registerReceiver(syncEngineBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver syncEngineBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DataManager.SYNC_ENGINE_ERROR)) {
                // TO DO: Implement client side error handling here
                logErrors();
            } else if (intent.getAction().equals(DataManager.SYNC_ENGINE_CONTENT_FILES_RECEIVED)) {
                Log.i(TAG, "received new file content");
//				SyncStatus syncStatus = dataManager.getSyncStatus();
                //TODO: hack
//				if (progressDialog.isShowing() && syncStatus.getStage().equals(SyncStatusStage.FETCH_CONTENT)) {
//					progressDialog.setMessage("Downloading content files!! Files left to download: " + syncStatus.getTotalCount());
//				}
                // updateContentFiles();
            } else if (DataManager.SYNC_COMPLETED.equals(intent.getAction())) {

                if (dataManager.isFirstSyncComplete(context)) {

                    // fetch content from smartstore and show select configs dialog
                    // only after first sync is completed
                    // not in all subsequent delta syncs
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean("SHOW_SELECT_CONFIGS", true)) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        showSelectConfigsDialog();
                    }
                } else {
                    //TODO: Need to understand how we are getting here
                    Log.e("TAG", "in onReceive, we should never get to this block");
                }
            }
        }
    };

    private BroadcastReceiver dsaBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("com.salesforce.dsa.SelectConfigClciked")) {
                showSelectConfigsDialog();
            } else if (intent.getAction().equals("com.salesforce.dsa.SyncClciked")) {
                progressDialog = new SyncProgressDialog(DigitalSalesAid.this);
                progressDialog.show();
            }
        }
    };

    private void registerDSAReceiver() {
        Log.i(TAG, "in registerDSAReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.salesforce.dsa.SelectConfigClciked");
        intentFilter.addAction("com.salesforce.dsa.SyncClciked");
        registerReceiver(dsaBroadcastReceiver, intentFilter);
    }

    private void showSelectConfigsDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Editor editor = prefs.edit();
        if (activeConfigList == null || activeConfigList.isEmpty()) {

            ModelDataFetcherTask.ModelDataFetcherCb<CN_DSA__c> callback = new ModelDataFetcherTask.ModelDataFetcherCb<CN_DSA__c>() {
                @Override
                public void onData(List<CN_DSA__c> data) {
                    DataUtils.checkAccessPermisson(data, new CheckAccessFilePermissionTask.OnCheckedCallBack<CN_DSA__c>() {
                        @Override
                        public void onCheckedCallBack(List<CN_DSA__c> list) {
                            Log.e(TAG, "onCheckedCallBack: " + list.toString() + list.size());
                            if (!list.isEmpty()) {
                                activeConfigList = list;
                                createAndShowDialog();
                                editor.putBoolean("SHOW_SELECT_CONFIGS", false);
                                editor.commit();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DigitalSalesAid.this);
                                builder.setMessage(DigitalSalesAid.this.getString(R.string.no_access))
                                        .setTitle(DigitalSalesAid.this.getString(R.string.no_access_title));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }

                    }, CN_DSA__c.class);

                }
            };
            DataUtils.fetchActiveMobileAppConfigs(callback);
        } else {
            createAndShowDialog();
        }

    }

    private void createAndShowDialog() {

        try {
            List<String> configNames = GuavaUtils.transform(activeConfigList, new GuavaUtils.Function<CN_DSA__c, String>() {
                public String apply(CN_DSA__c config) {
                    return config.getName();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.select_config));
            String[] items = configNames.toArray(new String[configNames.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int index) {
                    // pop to visual browser from menu browser
                    // when a different configuration is selected from menu browser
                    FragmentManager fm = getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                    }

                    // show selected configuration
                    activeConfig = activeConfigList.get(index);

                    // Save active config ID in prefs
                    // and load saved config on app relaunch
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DigitalSalesAid.this);
                    Editor editor = prefs.edit();
                    Log.e(TAG, "onClick: 0" + activeConfig.getId());
                    editor.putString(DSAConstants.Constants.ACTIVE_CONFIG_ID, activeConfig.getId());
                    editor.commit();
                    //fetch all category when activeConfigid saved
                    categoryLoaderTask.execute(0);
                    if (activeConfig != null) attachFragment();
//                    DataUtils.fetchCategoryMobileConfig(activeConfig, catMobileConfigCallback);
//                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            // Ignore any exceptions that can happen if the activity is already paused/destroyed
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    // onclick of buttons at the rootlevel
    // or anywhere in the visual view container
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.visual_view_container) {
            // v.setVisibility(View.GONE);

            crossfade(buttonViewContainer, visualViewContainer);

            activeCategory = null;
            // Toast.makeText(this, "Clicked on container!", Toast.LENGTH_SHORT).show();
        } else {
            Category__c category = (Category__c) v.getTag(R.id.TAG_TITLE_ID);
            // Toast.makeText(this, "Clicked on: " + category.getName(), Toast.LENGTH_SHORT).show();
//			DataUtils.fetchSubCategoriesForCategory(category);
            List<ContentVersion> list = DataUtils.fetchContentVersionsForCategory(category, this);
            // addCategoryView(category, 1);

            crossfade(visualViewContainer, buttonViewContainer);

            RelativeLayout categoryView = (RelativeLayout) visualMenuContainer.findViewWithTag("level" + 0);
            final ListView categoryListView = (ListView) categoryView.findViewById(R.id.categoryList);

            int position = activeCategoryList.indexOf(category);
            if (position != -1) {
                categoryListView.performItemClick(categoryListView.getChildAt(position), position, categoryListView.getItemIdAtPosition(position));
            }

            Log.i(TAG, "List of content versions " + list);
        }
    }

    private void crossfade(View fadeInView, final View fadeOutView) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
//        fadeInView.setAlpha(0f);
//        fadeInView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        fadeInView.animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fadeInView.setVisibility(View.VISIBLE);
                    }
                });
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        fadeOutView.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fadeOutView.setVisibility(View.GONE);
                    }
                });
    }

    private void addMainCategoryView() {
        visualMenuContainer.removeAllViews();
        final int level = 0;
        RelativeLayout categoryView = (RelativeLayout) (getLayoutInflater().inflate(R.layout.category_view_main, null));
        String currentTag = "level" + level;
        int id = getResources().getIdentifier(currentTag, "id", getPackageName());
        categoryView.setId(id);
        // categoryView.setId(View.generateViewId());

        final TextView categoryTitle = (TextView) categoryView.findViewById(R.id.categoryTitle);
        final ListView categoryListView = (ListView) categoryView.findViewById(R.id.categoryList);


        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(activeCategoryList, true);
        categoryListView.setAdapter(categoryListAdapter);
        categoryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        categoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Category__c selectedCategory = (Category__c) categoryListView.getAdapter().getItem(position);
                // categoryTitle.setText(selectedCategory.getName().replaceAll(".(?!$)", "$0\n"));
                Log.i(TAG, "selected category is: " + selectedCategory);
                activeCategory = selectedCategory;
//                List<Category__c> categories = CategoryUtils.getSubCategories(selectedCategory.getId());
                List<CN_DSA_Folder__c> categories = CategoryUtils.getSubCategories(selectedCategory.getId());
                if (categories != null && categories.size() > 0) {
                    categoryTitle.setText(selectedCategory.getName());
                    categoryTitle.setVisibility(View.VISIBLE);
                    categoryListView.setVisibility(View.GONE);
                    addCategoryView(selectedCategory, 1);
                } else {
                    categoryListView.setSelection(position);
                    categoryTitle.setVisibility(View.GONE);
                    categoryListView.setVisibility(View.VISIBLE);
                    categoryBackgroundImageView.setImageDrawable(null);
                    categoryOverlay.setBackgroundColor(Color.TRANSPARENT);
                    removeCategoryViews(1);
                    // Toast.makeText(getApplicationContext(), "No sub-categories for this category!", Toast.LENGTH_SHORT).show();
                }
                updateContentView(selectedCategory);
                visualContentContainer.setVisibility(View.VISIBLE);
            }

        });

        categoryTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryListView.setVisibility(View.VISIBLE);
                categoryTitle.setVisibility(View.GONE);
                removeCategoryViews(1);
                visualContentContainer.setVisibility(View.INVISIBLE);
                categoryBackgroundImageView.setImageDrawable(null);
                categoryOverlay.setBackgroundColor(Color.TRANSPARENT);
                //addCategoryView(category, level);
            }
        });

        // categoryTitle.setText(category.getName().replaceAll(".(?!$)", "$0\n"));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

        categoryView.setTag(currentTag);
        // categoryView.setTag(R.id.TAG_TITLE_ID, category);
        categoryView.setLayoutParams(layoutParams);

        visualMenuContainer.addView(categoryView);


    }

    private void removeCategoryViews(int level) {
        for (int i = level; i <= 7; i++) {
            View view = visualMenuContainer.findViewWithTag("level" + i);
            if (view != null) {
                Log.e(TAG, "find view for level: " + i);
                visualMenuContainer.removeView(view);
            }
        }
    }

    private void addCategoryView(final Category__c category, final int level) {

        List<CN_DSA_Folder__c> categories = CategoryUtils.getSubCategories(category.getId());
        CategoryMobileConfig__c categoryConfig = DataUtils.fetchCategoryConfigForCategoryAndAppConfigId(category, activeConfig.getId());

        if (categories == null) {
            Log.e(TAG, "we have zero categories");
            return;
        } else {
            Log.e(TAG, "categories size is: " + categories.size());
            if (categories.size() == 0) {
                return;
            }
        }

        removeCategoryViews(level);

        activeCategory = category;

        RelativeLayout categoryView = (RelativeLayout) (getLayoutInflater().inflate(R.layout.category_view, null));

        String currentTag = "level" + level;
        int id = getResources().getIdentifier(currentTag, "id", getPackageName());
        categoryView.setId(id);
        // categoryView.setId(View.generateViewId());

        final TextView categoryTitle = (TextView) categoryView.findViewById(R.id.categoryTitle);
        final ListView categoryListView = (ListView) categoryView.findViewById(R.id.categoryList);


        int categoryViewBackgroundColor = ContentUtils.getColorFromString(categoryConfig.getSub_Category_Background_Color__c(), Color.GRAY);

        updateCategoryBackground();

        categoryView.setBackgroundColor(categoryViewBackgroundColor);

//        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(categories, false);
//        categoryListView.setAdapter(categoryListAdapter);
        categoryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        categoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Category__c selectedCategory = (Category__c) categoryListView.getAdapter().getItem(position);
                // categoryTitle.setText(selectedCategory.getName().replaceAll(".(?!$)", "$0\n"));

                List<CN_DSA_Folder__c> categories = CategoryUtils.getSubCategories(selectedCategory.getId());
                if (categories != null && categories.size() > 0) {
                    categoryTitle.setText(selectedCategory.getName());
                    categoryTitle.setVisibility(View.VISIBLE);
                    categoryListView.setVisibility(View.GONE);
                    addCategoryView(selectedCategory, level + 1);
                } else {
                    categoryListView.setSelection(position);
                    removeCategoryViews(level + 1);
                    categoryTitle.setVisibility(View.GONE);
                    categoryListView.setVisibility(View.VISIBLE);
                    // Toast.makeText(getApplicationContext(), "No sub-categories for this category!", Toast.LENGTH_SHORT).show();
                }
                updateContentView(selectedCategory);
            }

        });

        categoryTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryListView.setVisibility(View.VISIBLE);
                categoryTitle.setVisibility(View.GONE);
                addCategoryView(category, level);
            }
        });

        // categoryTitle.setText(category.getName().replaceAll(".(?!$)", "$0\n"));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

        if (level > 0) {
            String leftTag = "level" + (level - 1);
            View leftView = visualMenuContainer.findViewWithTag(leftTag);
            if (leftView != null) {
                layoutParams.addRule(RelativeLayout.RIGHT_OF, leftView.getId());
                Log.e(TAG, "tag: " + leftTag + " id: " + leftView.getId());
            }
        }

        categoryView.setTag(currentTag);
        categoryView.setTag(R.id.TAG_TITLE_ID, category);
        categoryView.setLayoutParams(layoutParams);

        visualMenuContainer.addView(categoryView);

    }

    private void updateContentView(Category__c category) {
        CategoryMobileConfig__c categoryConfig = DataUtils.fetchCategoryConfigForCategoryAndAppConfigId(category, activeConfig.getId());

        float overlayAlpha = ContentUtils.getAlphaFromString(categoryConfig.getOverlayBgAlpha__c(), 0);
        int overlayBackgroundColor = ContentUtils.getColorFromString(categoryConfig.getOverlayBgColor__c(), Color.TRANSPARENT);

        // Always set background first and then alpha. Color includes alpha so the order is important
        categoryOverlay.setBackgroundColor(overlayBackgroundColor);
        categoryOverlay.setAlpha(overlayAlpha);

        Log.i(TAG, "alpha: " + overlayAlpha + "color: " + String.format("0x%8s", Integer.toHexString(overlayBackgroundColor)));

        category.getName();
        String headingText = categoryConfig.getGalleryHeadingText__c();
        int headingColor = ContentUtils.getColorFromString(categoryConfig.getGalleryHeadingTextColor__c(), Color.BLACK);

        TextView contentTitle = (TextView) visualViewContainer.findViewById(R.id.content_title);
        TextView contentSubtitle = (TextView) visualViewContainer.findViewById(R.id.content_subtitle);
        TextView contentDescription = (TextView) visualViewContainer.findViewById(R.id.content_description);

        contentTitle.setText(category.getName());

        contentSubtitle.setText(ContentUtils.getDisplayableString(headingText, ""));

        contentSubtitle.setTextColor(headingColor);

        int titleColor = ContentUtils.getColorFromString(categoryConfig.getOverlayTextColor__c(), Color.BLACK);
        contentTitle.setTextColor(titleColor);

        List<CN_DSA_Azure_File__c> contentVersions = DataUtils.fetchAllDsaFileForCatetories(category.getId());

        ListView listView = (ListView) visualContentContainer.findViewById(R.id.content_list);
        ContentListAdapter adapter = new ContentListAdapter(contentVersions, true);

        listView.setAdapter(adapter);

        contentDescription.setText(Html.fromHtml(ContentUtils.getDisplayableString(category.getDescription__c(), "")));
        contentDescription.setTextColor(titleColor);

        ListItemclickListener listener = new ListItemclickListener();
        listView.setOnItemClickListener(listener);
    }

    @Override
    public void downloadResult(boolean isSucess) {
        if (downloadDsaFileCallBack != null)
            downloadDsaFileCallBack.downloadResult(isSucess);
    }

    private class ListItemclickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getAdapter().getItem(position);
            if (object instanceof CN_DSA_Azure_File__c) {
                CN_DSA_Azure_File__c dsa_azure_file = (CN_DSA_Azure_File__c) object;
                downloadAndTriggrefresh(dsa_azure_file);
            }
        }
    }

    Executor THREAD_POOL_EXECUTOR = Executors.newFixedThreadPool(10);

    public void downloadAndTriggrefresh(CN_DSA_Azure_File__c dsa_azure_file) {
        String fileName = dsa_azure_file.getId() + "_" + dsa_azure_file.getName();
        String completeFileName = fileName + ContentUtils.fileNameSuffix(dsa_azure_file.getDsaFileTpye());
        tempFile = getTargetFile(fileName + TEMP_SUFFIX);
        targetFile = getTargetFile(completeFileName);
        long tempFileSize = ContentUtils.getExistFileSize(tempFile.getAbsolutePath());
        if (targetFile.exists()) {
            Intent contentIntent = ContentUtils.getContentIntent(DigitalSalesAid.this, targetFile, dsa_azure_file.getDsaFileTpye());
            if (contentIntent == null) {
                Toast.makeText(DigitalSalesAid.this, getResources().getString(R.string.can_not_open_this_file),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                DigitalSalesAid.this.startActivity(contentIntent);
            } catch (Exception e) {
                DigitalSalesAid.this.startActivity(ContentUtils.generateHtmlFileIntent(targetFile.getPath()));
            }
            triggerBehaviorRefresh(dsa_azure_file);
            return;
        }
        if (tempFile.exists()) {
            String currentProgress = Math.ceil((float) tempFileSize / Float.valueOf(dsa_azure_file.getFileSize()) * 100) + "%";
            String formatCurrentProgress = String.format(getString(R.string.being_downloading), currentProgress);
            Toast.makeText(DigitalSalesAid.this, formatCurrentProgress, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!DeviceNetworkUtils.isConnected(this)) {
            Toast.makeText(DigitalSalesAid.this, getString(R.string.network_disconnection), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!DeviceNetworkUtils.isWifi(this)) {
            AlertDialog netTipsDailog = new AlertDialog.Builder(this)
                    .setMessage(DigitalSalesAid.this.getString(R.string.net_tips))
                    .setNegativeButton(getString(R.string.no), null)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createDownloadTask(completeFileName, dsa_azure_file);
                        }
                    })
                    .show();
        } else {
            createDownloadTask(completeFileName, dsa_azure_file);
        }
        triggerBehaviorRefresh(dsa_azure_file);
    }

    private void createDownloadTask(String completeFileName, CN_DSA_Azure_File__c dsa_azure_file) {
        DownloadDsaFileTask downloadDsaFileTask = new DownloadDsaFileTask(DigitalSalesAid.this, tempFile, completeFileName);
        downloadDsaFileTask.setDownloadDsaFileCallBack(this);
        try {
            downloadFile(downloadDsaFileTask, dsa_azure_file.getId() + "/" + dsa_azure_file.getName());
            Toast.makeText(DigitalSalesAid.this, getString(R.string.download_start)
                    + dsa_azure_file.getDsaFileName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(DownloadDsaFileTask downloadDsaFileTask, String nameAndId) throws Exception {
        downloadDsaFileTask.executeOnExecutor(THREAD_POOL_EXECUTOR, nameAndId, tempFile.getAbsolutePath());
    }

    private void triggerBehaviorRefresh(CN_DSA_Azure_File__c dsa_azure_file) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AbInBevConstants.CNDsaAzueFields.CN_User__c, com.abinbev.dsa.model.User.getCurrentUser().getId());
            jsonObject.put(AbInBevConstants.CNDsaAzueFields.CN_DSA_Azure_File__c, dsa_azure_file.getId());
            jsonObject.put(AbInBevConstants.CNDsaAzueFields.CN_Access_Type__c, targetFile.exists()
                    ? "Open" : "Download");
            String dateTime = DateUtils.SERVER_DATE_TIME_FORMAT.format(Calendar.getInstance().getTime());
            jsonObject.put(AbInBevConstants.CNDsaAzueFields.CN_Datetime__c, dateTime);
            CN_DSA_Azure_File_Usage__c cn_dsa_azure_file_usage__c = new CN_DSA_Azure_File_Usage__c(jsonObject);
            cn_dsa_azure_file_usage__c.createRecord(dataManager);
            SyncUtils.TriggerRefresh(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deleteUploadedRecord();
    }

    private boolean deleteUploadedRecord() {
        String Filter = String.format("select {CN_DSA_Azure_File_Usage__c:_soup} from {CN_DSA_Azure_File_Usage__c}");
        JSONArray jsonArray = dataManager.fetchAllSmartSQLQuery(Filter);
        Log.e(TAG, "deleteUploadedRecord: " + jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONArray(i).getJSONObject(0);
                CN_DSA_Azure_File_Usage__c cn_dsa_azure_file_usage__c = new CN_DSA_Azure_File_Usage__c(jsonObject);
                String id = cn_dsa_azure_file_usage__c.getID();
                /*	After successfully submit data to backend, Backend record id will replace the local created id
                 field in the local database. (Currently the ID of local created record starts with ‘CN_’ + sequence number)*/
                if (!id.startsWith("CN_")) {
                    String del = "DEL" + id;
                    cn_dsa_azure_file_usage__c.setID(del);
                    cn_dsa_azure_file_usage__c.updateRecord(cn_dsa_azure_file_usage__c.getSoupEntryID(jsonObject), cn_dsa_azure_file_usage__c.toJson());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray jsonArray1 = dataManager.fetchAllSmartSQLQuery(Filter);
        for (int i = 0; i < jsonArray1.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray1.getJSONArray(i).getJSONObject(0);
                CN_DSA_Azure_File_Usage__c cn_dsa_azure_file_usage__c = new CN_DSA_Azure_File_Usage__c(jsonObject);
                String id = cn_dsa_azure_file_usage__c.getId();
                if (id.startsWith("DEL")) {
                    cn_dsa_azure_file_usage__c.deleteRecords(cn_dsa_azure_file_usage__c.getSoupEntryID(jsonObject));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonArray.length() == 0;
    }

    private final ModelDataFetcherTask.ModelDataFetcherCb<Category__c> categoryCallback = new ModelDataFetcherTask.ModelDataFetcherCb<Category__c>() {
        @Override
        public void onData(List<Category__c> data) {
            Log.e(TAG, "top level category count is: " + data.size());
            activeCategoryList = data;
//            attachFragment();
        }
    };

    // create a callback to fetch CategoryMobileConfig objects
    private final ModelDataFetcherTask.ModelDataFetcherCb<CN_DSA_Folder__c> catMobileConfigCallback = new ModelDataFetcherTask.ModelDataFetcherCb<CN_DSA_Folder__c>() {

        @Override
        public void onData(List<CN_DSA_Folder__c> data) {
            Log.e(TAG, "mobile config count is: " + data.size());
//            attachFragment();
//            List<String> catMobileAppConfigIds = GuavaUtils.transform(data, new GuavaUtils.Function<CN_DSA_Folder__c, String>() {
//                public String apply(CN_DSA_Folder__c config) {
//                    return config.getCategoryId__c();
//                }
//            });
//            // send the ids to the category fetcher
//            DataUtils.fetchTopLevelCategoriesForConfigs(catMobileAppConfigIds, categoryCallback);
        }
    };

    TimingLogger timings = new TimingLogger(TAG, "DSA");

    private class CategoryLoaderTask extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... urls) {
            timings.reset();
            String activeConfigId = PreferenceManager.getDefaultSharedPreferences(DigitalSalesAid.this)
                    .getString(DSAConstants.Constants.ACTIVE_CONFIG_ID, null);
            CategoryUtils.populateCategoryCacheHolder(DigitalSalesAid.this, activeConfigId);
            return 0;
        }

        protected void onPostExecute(Integer result) {
            timings.addSplit("populatedCategories");
            timings.dumpToLog();
            timings.reset();
            Log.i(TAG, "subcategories populated");
        }
    }

    private File getTargetFile(String id) {
        File file = new File(getExternalFilesDir(null), id);
        return file;
    }
}