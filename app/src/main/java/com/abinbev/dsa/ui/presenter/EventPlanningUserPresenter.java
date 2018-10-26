package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventPlanningUserPresenter extends AbstractEventPlanningPresenter {
    public static final String TAG = EventPlanningUserPresenter.class.getSimpleName();

    private String userId;

    public EventPlanningUserPresenter(String userId) {
        super();
        this.userId = userId;
    }

    @Override
    protected List<Event> getEvents() {
        return Event.getEventsForEventPlanningListByWhatId(userId);
    }
}
