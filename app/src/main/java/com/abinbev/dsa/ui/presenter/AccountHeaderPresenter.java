package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.utils.DSAConstants;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wandersonblough on 2/18/16.
 */
public class AccountHeaderPresenter implements Presenter<AccountHeaderPresenter.ViewModel> {

    private static final String TAG = AccountHeaderPresenter.class.getSimpleName();

    public interface ViewModel {
        void setAccount(Account account);

        void setAccountPhoto(Attachment attachment, String accountId);
    }

    private ViewModel viewModel;
    private CompositeSubscription subscription;
    private String accountId;


    public AccountHeaderPresenter(String accountId) {
        subscription = new CompositeSubscription();
        this.accountId = accountId;
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
        subscription.clear();
        getAccount();
        getAccountPhoto();
    }

    public void getAccount() {
        this.subscription.add(Observable.create((Observable.OnSubscribe<Account>) subscriber -> {
            Account account = new Account(DataManagerFactory.getDataManager().exactQuery(DSAConstants.DSAObjects.ACCOUNT, "Id", accountId));
            subscriber.onNext(account);
            subscriber.onCompleted();
        }).subscribeOn(AppScheduler.background())
        .observeOn(AppScheduler.main())
        .subscribe(new Subscriber<Account>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e);
            }

            @Override
            public void onNext(Account account) {
                viewModel.setAccount(account);
            }
        }));
    }

    public void getAccountPhoto() {
        this.subscription.add(Observable.create((Observable.OnSubscribe<Attachment>) subscriber -> {
            Attachment attachment = Attachment.getAccountPhotoAttachment(accountId);
            subscriber.onNext(attachment);
            subscriber.onCompleted();
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Attachment>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error retrieving account photo: ", e);
                    }

                    @Override
                    public void onNext(Attachment attachment) {
                        viewModel.setAccountPhoto(attachment, accountId);
                    }
                }));
    }

    @Override
    public void stop() {
        subscription.clear();
        this.viewModel = null;
    }
}
