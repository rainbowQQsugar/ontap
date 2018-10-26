package com.salesforce.dsa.app.ui.customview;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.dsa.app.R;

public class SyncProgressDialog extends AlertDialog {

    ImageView step1ImageView;
    ImageView step2ImageView;
    ImageView step3ImageView;
    ImageView step4ImageView;

    TextView syncStatusText;

    ProgressBar progressBar;

    int progressState;

    private Animation rotateAnimation;

    public SyncProgressDialog(Context context) {
        super(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        setCancelable(false);
        progressState = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.progress_view);

        step1ImageView = (ImageView) findViewById(R.id.step1Image);
        step2ImageView = (ImageView) findViewById(R.id.step2Image);
        step3ImageView = (ImageView) findViewById(R.id.step3Image);
        step4ImageView = (ImageView) findViewById(R.id.step4Image);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        syncStatusText = (TextView) findViewById(R.id.syncStatusText);

    }

    @Override
    protected void onStart() {
        super.onStart();

        registerProgressReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();

        getContext().unregisterReceiver(progressReceiver);
    }

    private void rotateImageView(ImageView imageView) {

        imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.sync_step_progress));

        // animate while syncing
        rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim);
        imageView.startAnimation(rotateAnimation);
    }

    @Override
    public void show() {
        super.show();

        SyncUtils.TriggerRefresh(getContext());
    }

    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Resources resources = getContext().getResources();

            if (intent.getAction().equals(DataManager.FETCH_METADATA_STARTED) && progressState < 1) {
                progressState = 1;
                progressBar.setProgress(100 / 8);
                rotateImageView(step1ImageView);
                syncStatusText.setText("Step 1: Queueing");

            } else if (intent.getAction().equals(DataManager.FETCH_METADATA_COMPLETED) && progressState < 2) {
                progressState = 2;
                progressBar.setProgress(100 / 8);
                step1ImageView.clearAnimation();
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));

            } else if (intent.getAction().equals(DataManager.FETCH_CONFIGURATION_STARTED) && progressState < 3) {
                progressState = 3;
                progressBar.setProgress(3 * 100 / 8);
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                rotateImageView(step2ImageView);
                syncStatusText.setText("Step 2: Configuring");

            } else if (intent.getAction().equals(DataManager.FETCH_CONFIGURATION_COMPLETED) && progressState < 4) {
                progressState = 4;
                progressBar.setProgress(5 * 100 / 8);
                step2ImageView.clearAnimation();
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step2ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));

            } else if (intent.getAction().equals(DataManager.FETCH_CONTENT_STARTED) && progressState < 5) {
                progressState = 5;
                progressBar.setProgress(5 * 100 / 8);
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step2ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                rotateImageView(step3ImageView);
                syncStatusText.setText("Step 3: Downloding Content");

            } else if (intent.getAction().equals(DataManager.FETCH_CONTENT_COMPLETED) && progressState < 6) {
                progressState = 6;
                progressBar.setProgress(5 * 100 / 8);
                step3ImageView.clearAnimation();
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step2ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step3ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));

            } else if (intent.getAction().equals(DataManager.SYNC_FINISHING) && progressState < 7) {
                progressState = 7;
                progressBar.setProgress(7 * 100 / 8);
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step2ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step3ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                rotateImageView(step4ImageView);
                syncStatusText.setText("Step 4: Finishing");

            } else if (intent.getAction().equals(DataManager.SYNC_FINISHED) && progressState < 8) {
                progressState = 8;
                progressBar.setProgress(100);
                step4ImageView.clearAnimation();
                step1ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step2ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step3ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                step4ImageView.setImageDrawable(resources.getDrawable(R.drawable.sync_step_done));
                dismiss();
            }
        }
    };

    private void registerProgressReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataManager.FETCH_CONFIGURATION_STARTED);
        intentFilter.addAction(DataManager.FETCH_CONFIGURATION_COMPLETED);
        intentFilter.addAction(DataManager.FETCH_METADATA_STARTED);
        intentFilter.addAction(DataManager.FETCH_METADATA_COMPLETED);
        intentFilter.addAction(DataManager.FETCH_CONTENT_STARTED);
        intentFilter.addAction(DataManager.FETCH_CONTENT_COMPLETED);
        intentFilter.addAction(DataManager.SYNC_FINISHING);
        intentFilter.addAction(DataManager.SYNC_FINISHED);
        getContext().registerReceiver(progressReceiver, intentFilter);
    }

}
