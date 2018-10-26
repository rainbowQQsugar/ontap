package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.model.VisitState;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public abstract class AbstractEventPlanningPresenter implements Presenter<AbstractEventPlanningPresenter.ViewModel> {
    public static final String TAG = AbstractEventPlanningPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Event> events);

        void setTaskData(List<Task> tasks);

        void setEventTypes(List<Event_Catalog__c> recordTypes);
    }

    private ViewModel viewModel;

    private CompositeSubscription compositeSubscription;
    private String selectedDate;
    private String userId;

    public AbstractEventPlanningPresenter() {
        super();
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void start() {
        this.compositeSubscription.clear();

        loadEventList();
        loadTaskList();
        loadEventCatalog(selectedDate);
    }

    protected List<Task> filterTaskList(List<Task> tasks, String selectedDate) {
        List<Task> filterTasks = new ArrayList<>();
        for (Task task : tasks) {
            String createdDateString = task.getCreatedDate();
            String dueDateString = task.getDueDate();
            Date selectDate = DateUtils.dateFromString(selectedDate);
            Date createDate = DateUtils.dateFromString(createdDateString);
            Date dueDate = DateUtils.dateFromString(dueDateString);
            if ((selectDate.getTime() >= createDate.getTime()) &&
                    (selectDate.getTime() <= dueDate.getTime()) && !Task.STATE_COMPLETED.equals(task.getState())) {
                filterTasks.add(task);
            }
        }
        return filterTasks;
    }

    protected List<Event> filterEventList(List<Event> events, String selectedDate) {
        List<Event> filterEvents = new ArrayList<>();
        for (Event event : events) {
            String startDateString = event.getStartDate();
            String endDateString = event.getEndDate();
            Date selectDate = DateUtils.dateFromString(selectedDate);
            Date startDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(startDateString));
            Date endDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(endDateString));

            if (isDailyVisit(event) && selectDate.getTime() == startDate.getTime()) {
                filterEvents.add(event);
            } else if (selectDate.getTime() >= startDate.getTime() && selectDate.getTime() <= endDate.getTime()) {
                filterEvents.add(event);
            }
        }
        return filterEvents;
    }

    public boolean isDailyVisit(Event event) {
        String startDate = DateUtils.dateToDateString(event.getStartDateTime());
        String endDate = DateUtils.dateToDateString(event.getEndDateTime());
        return startDate.equals(endDate);
    }

    protected void loadTaskList() {
        this.compositeSubscription.add(Observable.create(
                new Observable.OnSubscribe<List<Task>>() {
                    @Override
                    public void call(Subscriber<? super List<Task>> subscriber) {
                        List<Task> tasks = getTasks();
                        subscriber.onNext(tasks);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Task>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting events in event planning: ", e);
                    }

                    @Override
                    public void onNext(List<Task> tasks) {
                        viewModel.setTaskData(filterTaskList(tasks, selectedDate));
                    }
                }));

    }

    private void loadEventList() {
        this.compositeSubscription.add(Observable.create(
                new Observable.OnSubscribe<List<Event>>() {
                    @Override
                    public void call(Subscriber<? super List<Event>> subscriber) {
                        List<Event> events = getEvents();
                        subscriber.onNext(events);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Event>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting events in event planning: ", e);
                    }

                    @Override
                    public void onNext(List<Event> events) {
                        viewModel.setData(filterEventList(events, selectedDate));
                    }
                }));
    }

    protected abstract List<Event> getEvents();

    protected List<Task> getTasks() {
        return Task.TasksByUserId(userId);
    }

    private void loadEventCatalog(final String selectedDate) {
        this.compositeSubscription.add(Observable.create(
                new Observable.OnSubscribe<List<Event_Catalog__c>>() {
                    @Override
                    public void call(Subscriber<? super List<Event_Catalog__c>> subscriber) {
                        List<Event_Catalog__c> recordTypes = Event_Catalog__c.getActiveEventCatalog(selectedDate);
                        subscriber.onNext(recordTypes);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Event_Catalog__c>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting survey RecordTypes: ", e);
                    }

                    @Override
                    public void onNext(List<Event_Catalog__c> recordTypes) {
                        viewModel.setEventTypes(recordTypes);
                    }
                }));
    }


    @Override
    public void stop() {
        viewModel = null;
        compositeSubscription.clear();
    }
}
