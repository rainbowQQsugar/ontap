package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AppBaseDrawerActivity;
import com.abinbev.dsa.activity.LoginActivity;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.utils.SyncPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncEngine;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTouch;

/**
 * Created by wandersonblough on 1/15/16.
 */
public class SyncProgressView extends RelativeLayout implements SyncPresenter.ViewModel, SyncListener {

    private static final String SUPERSTATE = "superstate";
    private static final String IN_PROGRESS = "in_progress";
    @Bind(R.id.data)
    TextView data;
//
//    @Bind(R.id.object)
//    TextView objectField;

    @Bind(R.id.warning)
    TextView warning;

//    @Bind(R.id.progress)
//    ProgressBar progressBar;

    @Bind(R.id.background_image)
    ImageView backgroundImage;

    @Bind(R.id.imageView)
    ImageView imageView;

    SyncPresenter syncPresenter;
    private boolean inProgress;
    private boolean fullSync;
    private boolean showImage;
    private AlertDialog alertDialog;
    private Context context;

    public SyncProgressView(Context context) {
        this(context, null);
    }

    public SyncProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SyncProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        inflate(context, R.layout.sync_progress_view, this);
        ButterKnife.bind(this);
        Glide.with(context)
                .load(R.drawable.beer)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public void initDataManger() {
        DataManager dataManager = DataManagerFactory.getDataManager();
        dataManager.init(getContext());
    }

    public void startSync(boolean fullSync, boolean showImage) {
        this.fullSync = fullSync;
        this.showImage = showImage;
        inProgress = true;

        if (DeviceNetworkUtils.isConnected(getContext())) {
            data.setText("");
//            objectField.setText("");
            setVisibility(VISIBLE);
//            progressBar.setVisibility(GONE);
            imageView.setVisibility(VISIBLE);
            syncPresenter = new SyncPresenter(getContext());
            syncPresenter.setViewModel(this);
            backgroundImage.setVisibility(showImage ? VISIBLE : GONE);

            if (fullSync) {
                PreferenceUtils.putFullSyncComplete(false, getContext());
                data.setText(R.string.clearing_local_cata);
                warning.setVisibility(VISIBLE);
                imageView.setVisibility(VISIBLE);
//                objectField.setVisibility(GONE);
                syncPresenter.clearData();
            } else {
                warning.setVisibility(GONE);
                syncPresenter.start();
            }
        } else {
            onSyncFailure(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (syncPresenter != null) {
            syncPresenter.stop();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE && syncPresenter != null) {
            syncPresenter.stop();
        }
    }

    public void setLoggingOut() {
        data.setText(R.string.logging_out);
//        objectField.setVisibility(GONE);
        backgroundImage.setVisibility(GONE);
        warning.setVisibility(GONE);
        imageView.setVisibility(GONE);
        setVisibility(VISIBLE);
    }

    @Override
    public void dataCleared() {
        startSync(false, true);
    }

    @Override
    public void syncComplete() {
        // this is to handle the case where the screen powers off during sync
        // a value of false indicates that the delta sync is completed
        if (!PreferenceUtils.getTriggerDeltaSync(getContext())) {
            setVisibility(GONE);
            inProgress = false;
            Intent syncCompletedBroadcastIntent = new Intent(DataManager.SYNC_COMPLETED);
            getContext().sendBroadcast(syncCompletedBroadcastIntent);
        }
    }

    @Override
    public void syncUpdate(SyncStatus status) {
        if (status.getDescription() != null) {
            String description = status.getDescription();
            int breakPoint = description.indexOf(":");
            if (breakPoint != -1) {
                String state = description.substring(0, breakPoint + 1);
                String object = description.substring(breakPoint + 1, description.length());
//                data.setText(state);
                imageView.setVisibility(VISIBLE);
//                objectField.setText(object);
//                objectField.setVisibility(VISIBLE);
            }
//            else {
//                data.setText(description);
//                imageView.setVisibility(GONE);
//                objectField.setVisibility(GONE);
//            }
        }
    }

    @OnTouch(R.id.root)
    public boolean onTouchRoot() {
        return true;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    @Override
    public void onSyncCompleted() {
        if (DataManagerFactory.getDataManager().isFirstSyncComplete(getContext())) {
            setVisibility(GONE);
            inProgress = false;
        } else {
            showErrorDialog(null);
        }
    }

    @Override
    public void onSyncError(String message) {

    }

    @Override
    public void onSyncFailure(String message) {
        SyncStatus syncStatus = SyncEngine.getSyncStatus();
        String description = syncStatus.getDescription();

        if (description != null && (description.contains("Unknown soup") || description.contains("does not exist"))) {
            Log.e("SyncProgressView", "Got Unknown soup error. Need to Investigate!!");
            Log.e("SyncProgressView", description);
            fullSync = true;
        }
        //uncomment below line when debugging sync issues on first sync
        //fullSync = true;
        showErrorDialog(message);
    }

    @Override
    public void syncSkipped() {
        setVisibility(GONE);
        SyncUtils.cancelDeltaRefresh(getContext());
        inProgress = false;
        try {
            ((LoginActivity) getContext()).goToMainScreen();
        } catch (Exception e) {

        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPERSTATE, super.onSaveInstanceState());
        bundle.putBoolean(IN_PROGRESS, inProgress);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.inProgress = bundle.getBoolean(IN_PROGRESS);
            state = bundle.getParcelable(SUPERSTATE);
        }
        super.onRestoreInstanceState(state);
    }

    public void resume() {
        if (inProgress) {
            setVisibility(VISIBLE);
            if (SyncEngine.getSyncStatus().getStatus() != SyncStatus.SyncStatusState.INPROGRESS) {
                showErrorDialog(null);
            }
        } else {
            setVisibility(GONE);
        }
    }

    private void showErrorDialog(String message) {

        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        if (inProgress) {
            if (TextUtils.isEmpty(message)) {
                message = getContext().getString(R.string.sync_failure_message);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle(getContext().getString(R.string.sync_error))
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getContext().getString(R.string.try_again), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startSync(fullSync, showImage);
                        }
                    });

            if (DataManagerFactory.getDataManager().isFirstSyncComplete(getContext())) {
                builder.setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setVisibility(GONE);
                        SyncUtils.cancelDeltaRefresh(getContext());
                        inProgress = false;
                    }
                });
            } else {
                builder.setNegativeButton(getContext().getString(R.string.abi_logout), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setVisibility(GONE);
                        inProgress = false;
                        // log the user out ...
                        try {
                            ((AppBaseDrawerActivity) getContext()).logoutUser();
                        } catch (Exception e) {
                            ((LoginActivity) getContext()).logoutUser();
                        }
                    }
                });
            }

            alertDialog = builder.create();
            alertDialog.show();

        } else {
            if (TextUtils.isEmpty(message)) {
                message = getContext().getString(R.string.sync_error);
            }
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
