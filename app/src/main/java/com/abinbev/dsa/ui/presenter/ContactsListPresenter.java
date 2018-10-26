package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by lukaszwalukiewicz on 18.01.2016.
 */
public class ContactsListPresenter implements Presenter<ContactsListPresenter.ViewModel> {
    public static final String TAG = AccountCasesListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Contact> contacts);
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;

    public ContactsListPresenter(String accountId) {
        super();
        this.accountId = accountId;
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }
    @Override
    public void start() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(new Observable.OnSubscribe<List<Contact>>() {
            @Override
            public void call(Subscriber<? super List<Contact>> subscriber) {
                List<Contact> contacts = Contact.getContactsByAccountId(accountId);
                subscriber.onNext(contacts);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Contact>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all contacts: ", e);
                    }

                    @Override
                    public void onNext(List<Contact> contacts) {
                        viewModel.setData(contacts);
                    }
                });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
