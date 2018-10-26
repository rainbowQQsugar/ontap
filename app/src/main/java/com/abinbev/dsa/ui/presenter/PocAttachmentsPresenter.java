package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PocAttachmentsPresenter implements Presenter<PocAttachmentsPresenter.ViewModel> {
    private static final String TAG = PocAttachmentsPresenter.class.getSimpleName();
    public static final int SELECT_ATTACHMENT_REQUEST_CODE = 314;
    private ViewModel viewModel;
    private String accountId;
    private CompositeSubscription compositeSubscription;

    public interface ViewModel {
        void setData(List<Attachment> attachments);
        void setAccount(Account account);
    }

    public PocAttachmentsPresenter(String accountId) {
        super();
        this.accountId = accountId;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        fetchAttachments();
        getAccount();
    }

    public void fetchAttachments() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<Attachment>>() {
            @Override
            public void call(Subscriber<? super List<Attachment>> subscriber) {
                List<Attachment> attachments = Attachment.getAttachmentsForAccount(accountId);
                subscriber.onNext(attachments);
                subscriber.onCompleted();
            }
        })
                                            .subscribeOn(AppScheduler.background())
                                            .observeOn(AppScheduler.main())
                                            .subscribe(new Subscriber<List<Attachment>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all attachments: ", e);
                    }

                    @Override
                    public void onNext(List<Attachment> attachments) {
                        viewModel.setData(attachments);
                    }
                }));
    }

    public void getAccount() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<Account>() {
            @Override
            public void call(Subscriber<? super Account> subscriber) {
                subscriber.onNext(Account.getById(accountId));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Account>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting account: ", e);
                    }

                    @Override
                    public void onNext(Account account) {
                        viewModel.setAccount(account);
                    }
                }));
    }

    @Override
    public void stop() {
        viewModel = null;
        compositeSubscription.clear();
    }

    public void deleteAttachment(final String attachmentId, final String attachmentName) {

        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (TextUtils.isEmpty(attachmentId)) {
                    Attachment.deleteUnsyncedAttachment(accountId, attachmentName);
                } else {
                    Attachment.deleteAttachment(attachmentId);
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                                            .observeOn(AppScheduler.main())
                                            .subscribe(new Subscriber<Void>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    Log.e(TAG, "Error deleting attachment: ", e);
                                                }

                                                @Override
                                                public void onNext(Void aVoid) {
                                                    fetchAttachments();
                                                }
                                            }));
    }
}
