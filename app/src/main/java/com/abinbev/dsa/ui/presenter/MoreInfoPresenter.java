package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class MoreInfoPresenter implements Presenter<MoreInfoPresenter.ViewModel> {

    public static final String TAG = MoreInfoPresenter.class.getSimpleName();

    public interface ViewModel {
        void setLastVisit(String date);

        void setPrimaryContact(Contact contact);

        void setOwner(User user);

        void setAttachment(Attachment attachment, String accountId);
    }

    private ViewModel viewModel;
    private Event event;
    private Subscription subscription;
    private Subscription lastVisitSubscription;
    private Subscription ownerSubscription;
    private Subscription imageSubscription;

    public MoreInfoPresenter() {
        this(null);
    }

    public MoreInfoPresenter(Event event) {
        super();
        this.subscription = Subscriptions.empty();
        this.lastVisitSubscription = Subscriptions.empty();
        this.imageSubscription = Subscriptions.empty();
        this.ownerSubscription = Subscriptions.empty();
        this.event = event;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setEvent(Event event) {
        this.event = event;
        getMoreInfo();
    }

    @Override
    public void start() {
        getMoreInfo();
    }

    private void getMoreInfo() {
        if (event == null) {
            return;
        }
        subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<Contact>() {
            @Override
            public void call(Subscriber<? super Contact> subscriber) {
                Contact primaryContact = Account.getPrimaryContactForAccountId(event.getAccountId());
                subscriber.onNext(primaryContact);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Contact>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting contact: ", e);
                    }

                    @Override
                    public void onNext(Contact contact) {
                        viewModel.setPrimaryContact(contact);
                    }
                });

        lastVisitSubscription.unsubscribe();
        lastVisitSubscription = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String visitDate = Account.getLastVisitDateForAccountId(event.getAccountId());
                subscriber.onNext(visitDate);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting last visit date: ", e);
                    }

                    @Override
                    public void onNext(String date) {
                        viewModel.setLastVisit(date);
                    }
                });
        ownerSubscription.unsubscribe();
        ownerSubscription = Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                User user = User.getUserByUserId(event.getAccount().getOwnerId());
                subscriber.onNext(user);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                            }

                    @Override
                    public void onError(Throwable e) {
                            Log.e(TAG, "Error: ", e);
                            }

                    @Override
                    public void onNext(User user) {
                            viewModel.setOwner(user);
                            }
                            });
    }

    public void getAccountPhoto(final String accountId) {
        imageSubscription.unsubscribe();
        imageSubscription = Observable.create(new Observable.OnSubscribe<Attachment>() {
            @Override
            public void call(Subscriber<? super Attachment> subscriber) {
                Attachment attachment = Attachment.getAccountPhotoAttachment(accountId);
                subscriber.onNext(attachment);
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Attachment>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Attachment attachment) {
                        viewModel.setAttachment(attachment, accountId);
                    }
                });
    }

    @Override
    public void stop() {
        subscription.unsubscribe();
        lastVisitSubscription.unsubscribe();
        imageSubscription.unsubscribe();
        ownerSubscription.unsubscribe();
        viewModel = null;
    }


}
