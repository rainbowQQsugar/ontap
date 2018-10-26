package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Single;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class NotePresenter implements Presenter<NotePresenter.ViewModel> {

    public static final String TAG = NotePresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(Note note);
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;


    public NotePresenter(String accountId) {
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
        this.subscription = Single.fromCallable(
                () -> Note.getLatestNoteByParentId(accountId))
                    .subscribeOn(AppScheduler.background())
                    .observeOn(AppScheduler.main())
                    .subscribe(note -> viewModel.setData(note),
                            error -> Log.e(TAG, "Error in GettingLastNote: ", error));
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
