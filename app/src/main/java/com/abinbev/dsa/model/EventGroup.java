package com.abinbev.dsa.model;

import android.text.TextUtils;

import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Diana Błaszczyk on 12/12/17.
 */

public class EventGroup {

    private int visitsCount = 0;
    private int completedVisitsCount = 0;

    public List<Event> events;

    public EventGroup() {
        events = new ArrayList<>();
    }

    public Event getNextOpenEvent() {
        for (Event e : events) {
            if (e.getVisitState() == VisitState.open)
                return e;
        }
        return events.get(0);
    }

    public void sortByDate() {
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                if (!TextUtils.isEmpty(lhs.getCreatedDate()) && !TextUtils.isEmpty(rhs.getCreatedDate())) {
                    return DateUtils.dateFromDateTimeString(lhs.getCreatedDate())
                            .compareTo(DateUtils.dateFromDateTimeString(rhs.getCreatedDate()));
                } else if (TextUtils.isEmpty(lhs.getCreatedDate()) && TextUtils.isEmpty(rhs.getCreatedDate())) {
                    return 0;
                } else if (TextUtils.isEmpty(lhs.getCreatedDate())) {
                    return -1;
                }
                return 1;
            }
        });
    }

    public void setVisitsCount(int cnt) {
        this.visitsCount = cnt;
    }

    public int getVisitsCount() {
        return this.visitsCount;
    }

    public void setCompletedVisitsCount(int cnt) {
        this.completedVisitsCount = cnt;
    }

    public int getCompletedVisitsCount() {
        return this.completedVisitsCount;
    }
}
