package com.abinbev.dsa.adapter.events;

import java.util.Comparator;

/**
 * Created by jstafanowski on 30.01.18.
 */
class DistanceComparator implements Comparator<EventListItem> {

    @Override
    public int compare(EventListItem lhs, EventListItem rhs) {
        if (lhs.distance == null && rhs.distance == null) return 0;
        if (lhs.distance == null) return 1;
        if (rhs.distance == null) return -1;

        return lhs.distance.compareTo(rhs.distance);
    }
}
