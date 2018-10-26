package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Task;

import java.util.List;

public class EventPlanningAccountPresenter extends AbstractEventPlanningPresenter {
    public static final String TAG = EventPlanningAccountPresenter.class.getSimpleName();

    private String accountId;

    public EventPlanningAccountPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    protected List<Event> getEvents() {
        return Event.getEventsForEventPlanningList(accountId);
    }

    @Override
    protected List<Task> getTasks() {
        return Task.TasksByUserId(accountId);
    }
}
