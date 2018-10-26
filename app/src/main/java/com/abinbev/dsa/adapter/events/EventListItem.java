package com.abinbev.dsa.adapter.events;

import com.abinbev.dsa.model.Event;

/**
 * Created by jstafanowski on 30.01.18.
 */
class EventListItem {
    public final Event event;
    public final Float distance;
    public final int completedVisits;
    public final int totalVisits;
    public final int statusOrdinal;

    EventListItem(Event event, Float distance, int completedVisits, int totalVisits, int statusOrdinal) {
        this.event = event;
        this.distance = distance;
        this.completedVisits = completedVisits;
        this.totalVisits = totalVisits;
        this.statusOrdinal = statusOrdinal;
    }
}
