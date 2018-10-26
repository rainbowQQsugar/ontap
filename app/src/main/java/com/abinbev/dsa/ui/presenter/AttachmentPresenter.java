package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.AppScheduler;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by wandersonblough on 12/2/15.
 */
public class AttachmentPresenter implements Presenter<AttachmentPresenter.ViewModel> {

    public interface ViewModel {
        void setAttachments(List<Attachment> attachments);
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;

    @Inject
    Bus eventBus;

    protected AttachmentPresenter() {

    }

    public AttachmentPresenter(String accountId) {
        super();
        this.accountId = accountId;
        subscription = Subscriptions.empty();
        ((ABInBevApp) ABInBevApp.getAppContext()).getAppComponent().inject(this);
        eventBus.register(this);
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<List<Attachment>>() {
            @Override
            public void call(Subscriber<? super List<Attachment>> subscriber) {
                List<Attachment> attachmentList = Attachment.getAttachmentsForAccount(accountId);
                subscriber.onNext(attachmentList);
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Attachment>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(AttachmentPresenter.class.getSimpleName(), "Error Fetching Attachments", e);
                    }

                    @Override
                    public void onNext(List<Attachment> attachments) {
                        viewModel.setAttachments(attachments);
                    }
                });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
        eventBus.unregister(this);
    }

    @Subscribe
    public void onAttachmentSaved(AttachmentEvent.AttachmentSavedEvent attachmentSavedEvent) {
        start();
    }
}
