package com.abinbev.dsa.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.Presenter;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncEngine;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by wandersonblough on 1/15/16.
 */
public class SyncPresenter implements Presenter<SyncPresenter.ViewModel> {

    private static final String TAG = SyncPresenter.class.getSimpleName();

    private static final boolean DOWNLOAD_DEBUG = false;

    // Max time that application can spend in content download.
    private static final long MAX_DOWNLOAD_TIME = 10 * 60 * 1000L;

    // Maximum time without progress change that we can accept.
    private static final long MAX_NO_DOWNLOAD_TIME = 30 * 1000L;

    public interface ViewModel {

        void dataCleared();

        void syncComplete();

        void syncUpdate(SyncStatus status);

        void syncSkipped();
    }

    private ViewModel viewModel;
    private Subscription subscription;
    private Context context;
    private Handler handler;
    private DownloadManager downloadManager;

    private boolean isCheckingDownloadProgress = false;
    private long progressCheckStartTime;
    private long noProgressStartTime;
    private AlertDialog stopFetchDialog;

    private Runnable checkProgressRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = SystemClock.elapsedRealtime();

            // Stop sync if downloading takes too long.
            if (currentTime - progressCheckStartTime > MAX_DOWNLOAD_TIME) {
                stopDownloadProgressCheck();
                showStopFetchDialog();
            }
            else {
                // Stop sync if there was no change in download progress for specific amount of time.
                if (!isDownloading()) {
                    if (noProgressStartTime < 1) {
                        noProgressStartTime = currentTime;
                    }
                    else if (currentTime - noProgressStartTime > MAX_NO_DOWNLOAD_TIME) {
                        stopDownloadProgressCheck();
                        showStopFetchDialog();
                    }
                }
                else {
                    noProgressStartTime = 0;
                }
            }

            if (isCheckingDownloadProgress) {
                handler.postDelayed(this, 2000);
            }
        }

        private Map<Long, Long> lastDownloadProgress;

        private boolean isDownloading() {
            Cursor cursor = downloadManager.query(new DownloadManager.Query()
                    .setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING));
            Map<Long, Long> downloadProgress = getDownloadProgress(cursor);
            cursor.close();

            if (lastDownloadProgress == null || !lastDownloadProgress.equals(downloadProgress)) {
                if (DOWNLOAD_DEBUG) {
                    Log.i(TAG + ".DownloadProgress", "download progress has changed");
                    Log.i(TAG + ".DownloadProgress", "last: " + lastDownloadProgress);
                    Log.i(TAG + ".DownloadProgress", "current: " + downloadProgress);
                }
                lastDownloadProgress = downloadProgress;
                return true;
            }
            else {
                if (DOWNLOAD_DEBUG) {
                    Log.i(TAG + ".DownloadProgress", "no download probably");
                }
                lastDownloadProgress = downloadProgress;
                return false;
            }
        }

        private Map<Long, Long> getDownloadProgress(Cursor c) {
            Map<Long, Long> result = new HashMap<>();
            int idColumn = c.getColumnIndex(DownloadManager.COLUMN_ID);
            int bytesColumn = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            int lastModifiedColumn = c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);

            while (c.moveToNext()) {
                long id = c.getLong(idColumn);
                long bytes = c.getLong(bytesColumn);
                long timestamp = c.getLong(lastModifiedColumn);

                if (bytes > 0) { // No need to have progress equal 0
                    if (DOWNLOAD_DEBUG) {
                        Log.i(TAG + ".DownloadProgress", "id: " + id + " timestamp: " + timestamp + " bytes: " + bytes);
                    }
                    result.put(id, bytes);
                }
            }

            return result;
        }
    };

    public SyncPresenter(Context context) {
        this.context = context;
        subscription = Subscriptions.empty();
        this.handler = new Handler();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private void showStopFetchDialog() {
        stopFetchDialog = new AlertDialog.Builder(context)
                .setMessage(R.string.error_continue_sync)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        subscription.unsubscribe();
                        viewModel.syncSkipped();
                    }
                })
                .setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDownloadProgressCheck();
                    }
                })
                .create();
        stopFetchDialog.show();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<SyncStatus>() {
            @Override
            public void call(Subscriber<? super SyncStatus> subscriber) {
                boolean finished = false;
                boolean started = false;
                String description = null;
                SyncStatus syncStatus = SyncEngine.getSyncStatus();
                if (syncStatus.getStatus() != SyncStatus.SyncStatusState.INPROGRESS) {
                    SyncUtils.TriggerDeltaRefresh(context);
                }

                while (!finished) {
                    if (subscription.isUnsubscribed()) {
                        finished = true;
                        subscriber.onCompleted();
                    } else {
                        syncStatus = SyncEngine.getSyncStatus();
                        if (syncStatus.getStatus() == SyncStatus.SyncStatusState.INPROGRESS) {
                            started = true;
                            if (!syncStatus.getDescription().equals(description)) {
                                subscriber.onNext(syncStatus);
                                description = syncStatus.getDescription();
                            }
                        } else if (syncStatus.getStatus() == SyncStatus.SyncStatusState.COMPLETED && started) {
                            finished = true;
                            subscriber.onNext(syncStatus);
                            subscriber.onCompleted();
                        } else if (syncStatus.getStatus() == SyncStatus.SyncStatusState.NOT_SYNCING && started) {
                            finished = true;
                            subscriber.onNext(syncStatus);
                            subscriber.onCompleted();
                        }
                    }
                }

            }
        }).throttleLast(4, TimeUnit.SECONDS).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<SyncStatus>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Sync Completed");
                        viewModel.syncComplete();
                        stopDownloadProgressCheck();
                        hideFetchDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ABORTING SYNC", e);
                        stopDownloadProgressCheck();
                        hideFetchDialog();
                    }

                    @Override
                    public void onNext(SyncStatus status) {
                        Log.d(TAG, "onNext: STILL SYNCING");
                        Log.d(TAG, "onNext: DESCRIPTION: " + status.getDescription());
                        if (status.getStage() == SyncStatus.SyncStatusStage.FETCH_CONTENT && (stopFetchDialog == null || !stopFetchDialog.isShowing())) {
                            startDownloadProgressCheck();
                        }
                        else if (isCheckingDownloadProgress && status.getStage() != SyncStatus.SyncStatusStage.FETCH_CONTENT) {
                            Log.i(TAG, "switched from FETCH_CONTENT to a new stage: " + status.getStage());
                            stopDownloadProgressCheck();
                            hideFetchDialog();
                        }

                        viewModel.syncUpdate(status);
                    }
                });
    }

    @Override
    public void stop() {
        subscription.unsubscribe();
        stopDownloadProgressCheck();
        hideFetchDialog();
        viewModel = null;
    }

    public void clearData() {
        subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                DataManagerFactory.getDataManager().clearLocalData(context, true);
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

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        viewModel.dataCleared();
                        Log.d(TAG, "onNext: DATA CLEARED");
                    }
                });

    }

    private void startDownloadProgressCheck() {
        if (!isCheckingDownloadProgress) {
            isCheckingDownloadProgress = true;
            progressCheckStartTime = SystemClock.elapsedRealtime();
            noProgressStartTime = 0;
            handler.post(checkProgressRunnable);
        }
    }

    private void stopDownloadProgressCheck() {
        if (isCheckingDownloadProgress) {
            isCheckingDownloadProgress = false;
            noProgressStartTime = 0;
            handler.removeCallbacks(checkProgressRunnable);
        }
    }

    private void hideFetchDialog() {
        if (stopFetchDialog != null && stopFetchDialog.isShowing()) {
            stopFetchDialog.dismiss();
        }
    }
}
