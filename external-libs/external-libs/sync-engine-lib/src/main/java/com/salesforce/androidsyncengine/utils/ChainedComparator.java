package com.salesforce.androidsyncengine.utils;

import java.util.Comparator;

public class ChainedComparator<T> implements Comparator<T> {

    private final Comparator<T>[] comparators;

    public ChainedComparator(Comparator<T>... comparators) {
        this.comparators = comparators;
    }

    @Override
    public int compare(T a, T b) {
        for (Comparator<T> comparator: comparators) {
            int result = comparator.compare(a, b);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }
}
