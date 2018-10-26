package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.salesforce.dsa.BuildConfig;
import com.salesforce.dsa.location.LocationHandler;
import com.salesforce.dsa.location.LocationHandlerProvider;
import com.salesforce.dsa.location.LocationReceiver;

/**
 * Created by mewa on 6/12/17.
 */

public abstract class AbstractLocationAwarePresenter<T extends AbstractLocationAwarePresenter.LocationViewModel> extends AbstractPresenter<T> implements Presenter<T>, LocationReceiver {

//    private static final String TAG = "LocationAwarePresenter";

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationHandler locationHandler;

    public AbstractLocationAwarePresenter(LocationHandler locationHandler) {
        this.locationHandler = locationHandler;
    }

    public AbstractLocationAwarePresenter() { }

    @Override
    public void start() {
        if (locationHandler == null) {
            locationHandler = LocationHandlerProvider.createLocationHandler(this);
        }
    }

    @Override
    public void stop() {
        stopLocationUpdates();
        super.stop();
    }

    public interface LocationViewModel {
        Activity getActivity();
    }

    @Override
    public Activity getReceivingActivity() {
        return viewModel().getActivity();
    }

    @Override
    public int getFailureResolutionRequestCode() {
        return CONNECTION_FAILURE_RESOLUTION_REQUEST;
    }

    @Override
    public void handleUnresolvedError(int errorCode) {
        // TODO: extract error handler;
        // for now default implementation

        if (BuildConfig.CHINA_BUILD) {

            handleUnresolvedErrorForChinaBuild(errorCode);

            return;
        }

        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getReceivingActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);

        String errorMessageString = locationHandler.analyzeErrorCode(errorCode);
        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Show the error dialog in the DialogFragment
            errorDialog.show();
        }
        Toast.makeText(getReceivingActivity(), errorMessageString, Toast.LENGTH_LONG).show();
    }

    private void handleUnresolvedErrorForChinaBuild(int errorCode) {
        String errorMessageString = locationHandler.analyzeErrorCode(errorCode);
        if (errorMessageString == null) {
            return;
        }

        Toast.makeText(getReceivingActivity(), errorMessageString, Toast.LENGTH_LONG).show();
    }


    protected void startLocationUpdates() {
        if (locationHandler != null) {
            locationHandler.connect();
        }
    }

    protected void stopLocationUpdates() {
        if (locationHandler != null) {
            locationHandler.disconnect();
        }
    }

    protected LocationHandler getLocationHandler() {
        return locationHandler;
    }
}
