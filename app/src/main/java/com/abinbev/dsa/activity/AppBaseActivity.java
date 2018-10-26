package com.abinbev.dsa.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.di.component.AppComponent;
import com.abinbev.dsa.sync.DefaultSyncBroadcastReceiver;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.salesforce.dsa.location.LocationReceiver;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wandersonblough on 12/8/15.
 */
public abstract class AppBaseActivity extends AppCompatActivity implements LocationReceiver {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;


    RegisteredReceiver<SyncListener> syncEngineBroadcastReceiver = new DefaultSyncBroadcastReceiver();

    private SyncListener syncListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getPreferredOrientation());
        setContentView(getLayoutResId());
        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    private int getPreferredOrientation() {
        return getResources().getBoolean(R.bool.is10InchTablet) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncEngineBroadcastReceiver.register(this, this.syncListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncEngineBroadcastReceiver.unregister(this, this.syncListener);
    }

    public abstract int getLayoutResId();

    public AppComponent getAppComponent() {
        return ((ABInBevApp) getApplication()).getAppComponent();
    }

    public void setSyncListener(SyncListener syncListener) {
        this.syncListener = syncListener;
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
        if (com.salesforce.dsa.BuildConfig.CHINA_BUILD) {
            return;
        }

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
        // no action
    }

    @Override
    public void onConnected() {
        //no action
    }

}
