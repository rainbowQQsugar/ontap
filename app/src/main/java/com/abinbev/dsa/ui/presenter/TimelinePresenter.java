package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.model.VisitState;
import com.abinbev.dsa.utils.AbInBevConstants.DynamicFetch;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by mewa on 6/26/17.
 */

public class TimelinePresenter extends AbstractRxPresenter<TimelinePresenter.ViewModel> implements SyncListener {
    private static final String TAG = TimelinePresenter.class.getSimpleName();

    private final String accountId;
    private final RegisteredReceiver<SyncListener> syncReceiver;

    public interface ViewModel {

        void setEvents(List<Event> events);

        void setNotes(Note.NotesDetails notesDetails);

        Context getContext();
    }

    public TimelinePresenter(String accountId, RegisteredReceiver<SyncListener> syncReceiver) {
        this.accountId = accountId;
        this.syncReceiver = syncReceiver;
    }

    public void start(ViewModel viewModel) {
        setViewModel(viewModel);

        Context context = ABInBevApp.getAppContext();

        syncReceiver.register(context, this);

        Map<String, String> params = new HashMap<>();
        params.put("accountId", accountId);
        DynamicFetchEngine.fetchInBackground(context, DynamicFetch.TIMELINE_OPENED, params);

        refresh();
    }

    @Override
    public void start() {
        super.start();

        ViewModel vm = viewModel();
        if (vm != null) {
            start(vm);
        }
    }

    private void refresh() {
        subscriptions.clear();

        addSubscription(notesObservable(accountId)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .takeWhile(a -> viewModel() != null)
                .subscribe(notesDetails -> viewModel().setNotes(notesDetails))
        );

        addSubscription(visitsObservable(accountId)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .takeWhile(a -> viewModel() != null)
                .subscribe(events -> viewModel().setEvents(events))
        );
    }

    public void stop(ViewModel viewModel) {
        stopReceiver(ABInBevApp.getAppContext());
        super.stop();
    }

    @Override
    public void stop() {
        stop(viewModel());
        super.stop();
    }

    private Observable<List<Event>> visitsObservable(String accountId) {
        return Observable.just(Event.getVisitsNewerThan(90, accountId, VisitState.completed));
    }

    private Observable<Note.NotesDetails> notesObservable(String accountId) {
        return Note.notesByAccountId(accountId);
    }

    private void stopReceiver(Context context) {
        syncReceiver.unregister(context, this);
    }

    @Override
    public void onSyncCompleted() {
        stopReceiver(ABInBevApp.getAppContext());
        refresh();
    }

    @Override
    public void onSyncError(String message) {
        Log.e(TAG, "Sync error: " + message);
        stopReceiver(ABInBevApp.getAppContext());
    }

    @Override
    public void onSyncFailure(String message) {
        Log.e(TAG, "Sync failure: " + message);
        stopReceiver(ABInBevApp.getAppContext());
    }
}
