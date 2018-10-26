package com.abinbev.dsa.adapter.events;

import java.util.Comparator;

/**
 * Created by jstafanowski on 30.01.18.
 */
class StatusComparator implements Comparator<EventListItem> {

    @Override
    public int compare(EventListItem lhs, EventListItem rhs) {
        return lhs.statusOrdinal - rhs.statusOrdinal;
    }
}
