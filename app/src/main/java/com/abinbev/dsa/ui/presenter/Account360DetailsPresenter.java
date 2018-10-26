package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Morning_Meeting__c;
import com.abinbev.dsa.utils.AbInBevConstants.ProspectStatus;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.PermissionManager;
import com.salesforce.androidsdk.rest.ClientManager;

import okhttp3.OkHttpClient;
import rx.Single;
import rx.schedulers.Schedulers;

/**
 * Created by wandersonblough on 2/18/16.
 */
public class Account360DetailsPresenter extends AbstractRxPresenter<Account360DetailsPresenter.ViewModel> {

    private static final String TAG = Account360DetailsPresenter.class.getSimpleName();

    static class State {
        boolean isConversionPending;
        boolean isMorningMeetingFinished;
        boolean hasAccountPermissions;
        boolean hasOrdersPermissions;
    }

    public interface ViewModel {
        void setKpiVisible(boolean visible);

        void setPedidoVisible(boolean visible);

        void showAttachmentDownloadFailed();

        void showDownloadStarted();

        void refreshNotes();

        void refreshPerformanceIndicators();

        void refreshAttachments();

        void refreshPedidos();

        void refreshKpis();

        void hideAllViews();

        Activity getActivity();
    }

    private ClientManager clientManager;

    private OkHttpClient okHttpClient;

    private String accountId;

    private ABInBevApp appContext;


    public Account360DetailsPresenter(String accountId) {
        this.accountId = accountId;
        this.appContext = (ABInBevApp) ABInBevApp.getAppContext();
        this.clientManager = appContext.createClientManager();
        this.okHttpClient = appContext.createOkHttpClient(clientManager);
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        addSubscription(Single.fromCallable(
                () -> {
                    PermissionManager permissionManager = PermissionManager.getInstance();
                    State status = new State();

                    Account account = Account.getById(accountId);
                    if (account != null) {
                        status.isConversionPending = ProspectStatus.SUBMITTED.equals(
                                account.getProspectStatus());

                        if (!status.isConversionPending) {
                            status.isMorningMeetingFinished = Morning_Meeting__c.isMorningMeetingFinished();

                            if (status.isMorningMeetingFinished) {
                                status.hasAccountPermissions = permissionManager.hasPermission(PermissionManager.ACCOUNT_KPIS);
                                status.hasOrdersPermissions = permissionManager.hasPermission(PermissionManager.ORDERS_TILE);
                            }
                        }
                    }

                    return status;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AppScheduler.main())
                .subscribe(
                        this::setState,
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    public void viewAttachment(final Attachment attachment) {
        if (attachment.isFileDownloaded(appContext, accountId)) {
            openAttachment(attachment);
        }
        else {
            viewModel().showDownloadStarted();
            downloadAttachment(attachment);
        }
    }

    private void downloadAttachment(final Attachment attachment) {
        addSubscription(Single.fromCallable(
                () -> {
                    attachment.invalidateFilePath();
                    AttachmentUtils.downloadAttachmentFile(clientManager, okHttpClient,
                            appContext, attachment);
                    return attachment;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AppScheduler.main())
                .subscribe(
                        this::openAttachment,
                        error -> {
                            viewModel().showAttachmentDownloadFailed();
                            Log.e(TAG, "onError: ", error);
                        }
                ));
    }

    void openAttachment(Attachment attachment) {
        if (ContentUtils.isNull_OR_Blank(attachment.getId())) {
            AttachmentUtils.openUnsyncedAccountAttachment(attachment, viewModel().getActivity(), accountId);
        }
        else {
            AttachmentUtils.openAttachment(attachment, viewModel().getActivity(), accountId);
        }
    }

    void setState(State state) {
        if (state.isConversionPending) {
            viewModel().hideAllViews();
        }
        else if (!state.isMorningMeetingFinished) {
            viewModel().hideAllViews();
        }
        else {
            if (state.hasAccountPermissions) {
                viewModel().setKpiVisible(true);
                viewModel().refreshKpis();
            } else {
                viewModel().setKpiVisible(false);
            }

            if (state.hasOrdersPermissions) {
                viewModel().setPedidoVisible(true);
                viewModel().refreshPedidos();
            } else {
                viewModel().setPedidoVisible(false);
            }

            viewModel().refreshNotes();
            viewModel().refreshPerformanceIndicators();
            viewModel().refreshAttachments();
        }
    }
}
