package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.salesforce.androidsdk.rest.ClientManager;

import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by lukaszwalukiewicz on 18.01.2016.
 */
public class AttachmentsListPresenter implements Presenter<AttachmentsListPresenter.ViewModel> {
    private static final String TAG = AttachmentsListPresenter.class.getSimpleName();

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;
    private Activity activity;

    private ClientManager clientManager;

    private OkHttpClient okHttpClient;

    public interface ViewModel {
        void setData(List<Attachment> attachments);
    }

    public AttachmentsListPresenter(Activity activity, String accountId) {
        super();
        this.accountId = accountId;
        this.activity = activity;
        this.subscription = Subscriptions.empty();

        ABInBevApp appContext = (ABInBevApp) activity.getApplicationContext();
        this.clientManager = appContext.createClientManager();
        this.okHttpClient = appContext.createOkHttpClient(clientManager);
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(
                new Observable.OnSubscribe<List<Attachment>>() {
                    @Override
                    public void call(Subscriber<? super List<Attachment>> subscriber) {
                        List<Attachment> attachments = Attachment.getAttachmentsForAccount(accountId);

                        // Preload file paths.
                        for (Attachment attachment : attachments) {
                            attachment.getFilePath(ABInBevApp.getAppContext(), accountId);
                        }

                        subscriber.onNext(attachments);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Attachment>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all attachments: ", e);
                    }

                    @Override
                    public void onNext(List<Attachment> attachments) {
                        viewModel.setData(attachments);
                    }
                });
    }

    @Override
    public void stop() {
    }

    public void onDestroy() {
        viewModel = null;
        subscription.unsubscribe();

    }

    public void onAttachmentClicked(final Attachment attachment) {
        if (attachment.isFileDownloaded(activity, accountId)) {
            openAttachment(attachment);
        }
        else {
            showToast(R.string.toast_downloading_attachment, Toast.LENGTH_SHORT);
            attachment.invalidateFilePath();

            subscription.unsubscribe();
            subscription = Observable.create(
                    new Observable.OnSubscribe<Attachment>() {
                        @Override
                        public void call(Subscriber<? super Attachment> subscriber) {
                            try {
                                AttachmentUtils.downloadAttachmentFile(clientManager, okHttpClient,
                                        activity.getApplicationContext(), attachment);
                                subscriber.onNext(attachment);
                                subscriber.onCompleted();
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AppScheduler.main())
                    .subscribe(new Subscriber<Attachment>() {
                        @Override
                        public void onCompleted() { }

                        @Override
                        public void onError(Throwable e) {
                            showToast(R.string.toast_attachment_download_failed, Toast.LENGTH_SHORT);
                            Log.e(TAG, "onError: ", e);
                        }

                        @Override
                        public void onNext(Attachment result) {
                            openAttachment(result);
                        }
                    });
        }
    }

    private void openAttachment(Attachment attachment){
        if (ContentUtils.isNull_OR_Blank(attachment.getId())) {
            AttachmentUtils.openUnsyncedAccountAttachment(attachment, activity, this.accountId);
        } else {
            AttachmentUtils.openAttachment(attachment, activity, this.accountId);
        }
    }

    private void showToast(int stringRes, int length) {
        Toast.makeText(activity, stringRes, length).show();
    }
}
