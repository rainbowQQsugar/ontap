package com.abinbev.dsa.ui.presenter;


import android.util.Log;

import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public abstract class AbstractTasksListPresenter implements Presenter<AbstractTasksListPresenter.ViewModel>  {
    public static final String TAG = AbstractTasksListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setTasks(List<Task> tasks);
    }
    private Subscription subscription;
    private ViewModel viewModel;

    public AbstractTasksListPresenter() {
        super();
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(new Observable.OnSubscribe<List<Task>>() {
            @Override
            public void call(Subscriber<? super List<Task>> subscriber) {
                List<Task> tasks = getTasks();

                String objectType = AbInBevConstants.AbInBevObjects.TASK;
                String[] fields = {
                        AbInBevConstants.TaskFields.STATUS,
                        AbInBevConstants.TaskFields.SUBJECT
                };

                TranslatableSFBaseObject.addTranslations(tasks, objectType, fields);

                subscriber.onNext(tasks);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Task>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting tasks: ", e);
                    }

                    @Override
                    public void onNext(List<Task> tasks) {
                        viewModel.setTasks(tasks);
                    }
                });
    }

    protected abstract List<Task> getTasks();

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
