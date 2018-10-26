package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.util.Log;

import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class InitialQueueActionDialogPresenter implements Presenter<InitialQueueActionDialogPresenter.ViewModel> {

    public static final String TAG = InitialQueueActionDialogPresenter.class.getSimpleName();

    public interface ViewModel {
        void close();

        void showDeletingProgress();
    }

    private ViewModel viewModel;
    private CompositeSubscription subscription;
    private Activity activity;

    public InitialQueueActionDialogPresenter(Activity activity) {
        super();
        this.activity = activity;
        this.subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {

    }

    public void onDeleteClicked() {
        viewModel.showDeletingProgress();
        subscription.add(Observable.create(
                new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        DataManager dataManager = DataManagerFactory.getDataManager();
                        List<QueueObject> queueObjectList = DataManagerFactory.getDataManager().getQueueRecords();
                        for (QueueObject queueObject : queueObjectList) {
                            dataManager.deleteQueueRecordFromClient(queueObject.getSoupEntryId(), true);
                        }

                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        viewModel.close();
                    }

                    @Override
                    public void onNext(Boolean result) {
                        viewModel.close();
                    }
                }));
    }

    public void onSendClicked() {
        SyncUtils.TriggerRefresh(activity);
        viewModel.close();
    }


    @Override
    public void stop() {
        subscription.unsubscribe();
        viewModel = null;
    }
}
