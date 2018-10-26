package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class NotesListPresenter implements Presenter<NotesListPresenter.ViewModel> {

    public static final String TAG = NotesListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Note> notes, Map<String, User> notesCreators);
        void setAccount(Account account);
    }
    private ViewModel viewModel;
    private CompositeSubscription subscription;
    private String accountId;

    public NotesListPresenter(String accountId) {
        super();
        this.accountId = accountId;
        this.subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.clear();
        getNotes();
        getAccount();
    }

    public void getNotes() {
        this.subscription.add(Note.notesByAccountId(accountId)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Note.NotesDetails>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting notes: ", e);
                    }

                    @Override
                    public void onNext(Note.NotesDetails result) {
                        viewModel.setData(result.notes, result.users);
                    }
                }));
    }

    public void getAccount() {
        this.subscription.add(Observable.create(
                new Observable.OnSubscribe<Account>() {
                    @Override
                    public void call(Subscriber<? super Account> subscriber) {
                        Account account = Account.getById(accountId);
                        subscriber.onNext(account);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Account>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting notes: ", e);
                    }

                    @Override
                    public void onNext(Account account) {
                        viewModel.setAccount(account);
                    }
                }));
    }

    @Override
    public void stop() {
        subscription.clear();
        viewModel = null;
    }
}
