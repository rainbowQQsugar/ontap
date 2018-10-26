package com.abinbev.dsa.ui.presenter;

import android.content.Context;

import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import rx.subscriptions.CompositeSubscription;

public class MainScreenPresenter implements Presenter<MainScreenPresenter.ViewModel> {
    public static final String TAG = MainScreenPresenter.class.getSimpleName();

    public interface ViewModel {
        Context getContext();
        void promptQueueDataUpload();
        void promptManifestUpdate();
    }

    final CompositeSubscription subscription;
    final boolean shouldShowQueueDialog;
    ViewModel viewModel;

    public MainScreenPresenter(boolean showQueueDialog) {
        super();
        this.subscription = new CompositeSubscription();
        this.shouldShowQueueDialog = showQueueDialog;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;

        if (shouldShowManifestUpdateDialog()) {
            viewModel.promptManifestUpdate();
        }
        else if (shouldShowQueueDialog) {
            viewModel.promptQueueDataUpload();
        }
    }

    @Override
    public void start() {
        subscription.clear();
    }

    @Override
    public void stop() {
        subscription.clear();
    }

    public void onDestroy() {
        viewModel = null;
    }

    private Context getContext() {
        return viewModel.getContext();
    }

    private boolean shouldShowManifestUpdateDialog() {
        return PreferenceUtils.getManifestVersion(getContext()) == 0;
    }
}
