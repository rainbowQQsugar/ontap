package com.abinbev.dsa.ui.presenter;

import android.content.DialogInterface;
import android.util.Log;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by lukaszwalukiewicz on 22.01.2016.
 */
public class NoteDetailPresenter implements Presenter<NoteDetailPresenter.ViewModel> {
    public static final String TAG = NoteDetailPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(Note note);
        void askIfClose();
    }

    private ViewModel viewModel;
    private String noteId;
    private String accountId;
    private Subscription subscription;

    public NoteDetailPresenter(String noteId, String accountId) {
        super();
        this.noteId = noteId;
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
        this.subscription = Observable.create(new Observable.OnSubscribe<Note>() {
            @Override
            public void call(Subscriber<? super Note> subscriber) {
                Note note = null;
                if (noteId != null){
                    note = Note.getById(noteId);
                } else if (accountId != null){
                    note = Note.createNote(accountId);
                }
                subscriber.onNext(note);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Note>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in note: ", e);
                    }

                    @Override
                    public void onNext(Note note) {
                        viewModel.setData(note);
                    }
                });
    }

    @Override
    public void stop() {
        subscription.unsubscribe();
        viewModel = null;
    }

    public void onBackPressed() {
        if (viewModel != null) {
            viewModel.askIfClose();
        }
    }
}
